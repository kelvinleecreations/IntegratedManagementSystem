document.addEventListener("DOMContentLoaded", () => {
  const yearSelect = document.getElementById("year-select");
  const monthSelect = document.getElementById("month-select");

  // asynchronous function to perform fetches without interrupting user interface
  async function getAndDisplayApprovedLeave() {
    const year = yearSelect.value;
    const month = monthSelect.value;

    if (!year || !month) return;

    try {
      // performs a background request to rest controller
      // await to wait for server reply before continuing
      const response = await fetch('/api/leave-cal-data/analysis?month=' + month + '&year=' + year);

      // if unauthorised, redirect to welcome page
      if (response.status === 401) {
        alert("User no longer exist. Redirecting you to login.");
        window.location.href = "/logout/accountnotfound";
        return;
      }

      // converts the raw data from server into a JS array matching Calendar DTO structure
      const leaves = await response.json();

      addLeaveIndicators(leaves);
    }
    catch(e) {
      console.error("Error fetching leave calendar data:", e);
    }
  };

  getAndDisplayApprovedLeave();

  function addLeaveIndicators(leaves) {
    // remove all leave indicators
    document.querySelectorAll(".leave-indicator").forEach(i => i.remove());

    // set up colour name list and its index counter
    const userColourList = ["seagreen", "cornflowerblue", "darkred", "grey", "darkviolet", "olive"];
    let userColourListCounter = 0;

    const userColourHashMap = new Map();

    // select all day calendar grids from html
    const dayElements = document.querySelectorAll(".calendar-grid-section .day");

    // loop to go through each calendar grid
    dayElements.forEach(d => {
      // looks for span tag
      const day = d.querySelector("span");

      // skips blank days and continues
      if (!day) {
        return;
      }

      // get the text within the span tag, removes any whitespaces, convert string into an integer number
      const dayNum = parseInt(day.innerText.trim(), 10);

      // Filter approved leaves based on day attribute in the dto and calendar grid day number
      const dayLeave = leaves.filter(l => l.day === dayNum);

      // loop for the filtered list of leaves for each calendar day
      dayLeave.forEach(leave => {
        // create an empty <div>
        const leaveCalDiv = document.createElement("div");
        // set css name
        leaveCalDiv.className = "leave-indicator";
        // construct a formatted text string and place it within the newly created empty <div>
        leaveCalDiv.innerText = leave.name + " " + leave.durationStr + " (" + leave.leaveType + " - " + leave.leaveDuration + ") ";

        // assign and store colour using the colour map
        // if the name does not exist as key in the hashmap, assign and store the colour as value
        // increment the counter that serves as index for the colorNames list
        // no overflowing issues because the team size is not large enough yet
        if (!userColourHashMap.has(leave.name)) {
          userColourHashMap.set(leave.name, userColourList[userColourListCounter]);
          userColourListCounter++;
        }

        // get the value (colour) based on the key (name)
        leaveCalDiv.style.backgroundColor = userColourHashMap.get(leave.name);

        // add in this newly created <div> into the calendar grid (parent container)
        d.appendChild(leaveCalDiv);
      });

    });
  }
});