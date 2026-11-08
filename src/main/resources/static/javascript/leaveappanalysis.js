document.addEventListener("DOMContentLoaded", () => {
  const aId = document.getElementById("formApplicationId").value;
  const uId = document.getElementById("formUserId").value;

  fetch('/api/leave-app-data/analysis?applicationId=' + aId + '&userId=' + uId)
    .then(res => {
      if (!res.ok) {
        // if unauthorised, redirect to welcome page
        if (res.status === 401) {
          alert("User no longer exist. Redirecting you to login.");
          window.location.href = "/logout/accountnotfound";
          return;
        }
        if (res.status === 404) {
          alert("Leave Application not found");
          return [];
        }

        alert("Unexpected error occurred. Unable to display data.");
        return [];
      }

      return res.json();
    })
    .then(clashes => {
      if (clashes.length == 0) {
        return;
      }

      const clashList = document.getElementById('subordinateClashList');
      clashList.innerHTML = "";

      clashes.forEach(c => {
        clashList.innerHTML += '<li><b>' + c.name + '</b> has a leave application with status <u>' + c.applicationStatus + '</u> from ' + c.startDate + ' to ' + c.endDate + ' (' + c.leaveDuration + ')</li>';
      });
    })
    .catch(e => console.error("Error generating clash list:", e));
});