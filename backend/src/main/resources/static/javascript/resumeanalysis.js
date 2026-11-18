document.getElementById('resumeReviewForm').addEventListener('submit', async function review(e) {
  // prevent navigation to a new page or reload page
  e.preventDefault();

  // revoke any url created during the previous event
  if (this._prevBlobUrls) {
    this._prevBlobUrls.forEach(u => URL.revokeObjectURL(u));
  }

  const formData = new FormData(this);
  const submitButton = document.getElementById('submit-button');
  const reviewResultsDiv = document.getElementById('review-results-container');
  const errorbanner = document.getElementById('flash-attribute-section-by-js');

  errorbanner.style.display = "none";

  // take the uploaded pdf files from the form and generate links
  const pdfFileUrlHashMap = new Map();
  const uploadedFileName = this.querySelector('input[type="file"]').name;
  const uploadedFiles = this.querySelector('input[type="file"]').files;

  formData.delete(uploadedFileName);

  // add an index in front of the name, to allow cases for same name resume uploads
  Array.from(uploadedFiles).forEach((f, idx) => {
    pdfFileUrlHashMap.set(`${idx}_${f.name}`, URL.createObjectURL(f));
    formData.append(uploadedFileName, f, `${idx}_${f.name}`);
  });

  // keep a snapshot of the urls created
  this._prevBlobUrls = Array.from(pdfFileUrlHashMap.values());

  // disable submit button
  submitButton.disabled = true;

  let rateLimitExceeded = false;

  try {
    const res = await fetch(this.action, {
      method: this.method,
      body: formData
    });

    // if unauthorised, redirect to welcome page
    if (res.status === 401) {
      alert("User no longer exist. Redirecting you to login.");
      window.location.href = "/logout/accountnotfound";
      return;
    }

    // show loading message
    reviewResultsDiv.innerHTML = `<div class="loading-msg-section">Reviewing and Ranking Resumes... Please wait for a moment.</div>`;

    const reviewResults = await res.json();

    if (!res.ok) {
      reviewResultsDiv.innerHTML = `<div class="error-msg-section">${reviewResults.error || "Failure in reviewing the uploaded resume(s)."}</div>`;

      // if too many requests
      if (res.status === 429) {
        // to skip the finally block, prevent re-enabling of button
        rateLimitExceeded = true;

        errorbanner.style.display = "block";
        errorbanner.innerText = "Daily AI Usage Limit Reached: Please try again tomorrow.";

        submitButton.disabled = true;
        submitButton.textContent = "Daily AI Usage Limit Reached";
        
        return;
      }

      return;
    }

    // remove the loading message before displaying the review result
    reviewResultsDiv.innerHTML = "";

    // add disclaimer message
    reviewResultsDiv.innerHTML = `
    <div class="disclaimer-msg-section">
      Review Completed: Displaying review and ranking result. 
      <i>Note: The AI Reviewer may sometimes make mistakes.</i>
    </div>`

    reviewResults.forEach((r, idx) => {
      // get the pdf url using r.pdfFileName
      const pdfUrl = pdfFileUrlHashMap.get(r.pdfFileName) ? `<a href="${pdfFileUrlHashMap.get(r.pdfFileName)}" target="_blank">View PDF</a>` : "";

      reviewResultsDiv.innerHTML += `
        <div class="review-candidate-section">
          <h3>${idx + 1}: ${r.candidateName} - Score: ${r.score.toFixed(2)}/100</h3>
          <p><strong>Candidate's Resume:</strong> ${pdfUrl}</p>
          <p><strong>---Score Assessment---</strong></p>
          <p><strong>Score Figures Explained:</strong></p>
          <p>Matching Score (Derived from Matching Percentage): ${r.jobDescriptionMatchingPercentage.toFixed(2)}/100 [50%]</p>
          <p>Experience Score: ${r.experienceScore.toFixed(2)}/100 [50%]</p> 
          <p><strong>---Statistics For Role Compatibility---</strong></p>
          <p><strong>Matching Percentage:</strong> ${r.jobDescriptionMatchingPercentage.toFixed(2)}%</p>
          <p><strong>Matching Word(s):</strong> ${r.matchingWords.join(', ') || "None"}</p>
          <p><strong>Missing Word(s):</strong> ${r.missingWords.join(', ') || "None"}</p>
          <p><strong>---Analysis---</strong></p>
          <p><strong>Strengths:</strong> ${r.strengths.join(', ') || "None"}</p>
          <p><strong>Weaknesses:</strong> ${r.weaknesses.join(', ') || "None"}</p>
          <p><Strong>Summary:</strong> ${r.summary}</p>
        </div>`
    })
  } catch (e) {
    reviewResultsDiv.innerHTML = `<div class="error-msg-section">${e.message}</div>`

  } finally {
    if (!rateLimitExceeded) {
      // re-enable submit button
      submitButton.disabled = false;
    }
  }
});