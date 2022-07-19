// Functionality for dynamically resizeable table
$(document).ready(function () {
    // Function to resize the divs that are used to resize the columns (make divs same height as table).
    // This needs to be done when a search filter is applied or the column widths are changed (both may change table height)
    function resizeColumnResizers() {
        let divsToResize = document.getElementsByClassName('column-resizer-div');
        for (let i = 0; i < divsToResize.length; i++) {
            divsToResize[i].style.height = document.getElementById('myTable').offsetHeight - 5 + 'px';
        }
    }
    // Function to make the table columns resizeable
    function resizeableTable() {
        var table = document.getElementById('myTable'); // get the table
        var row = table.getElementsByTagName('tr')[0]; // get the first row (the table headers)
        var cols = row.children;
        var tableHeight = table.offsetHeight;
        for (let i = 0; i < cols.length; i++) {
            var div = createDiv(tableHeight); // create a div to the right of each column
            cols[i].appendChild(div);
            cols[i].style.position = 'relative';
            addListeners(div); // add the event listeners to each div
        }
    }
    // Function to add the event listeners for mousedown, mouseup, moveover, mouseout, and mousemove events
    function addListeners(div) {
        var pageX, curCol, nxtCol, curColWidth, nxtColWidth;
        // mousedown event
        div.addEventListener('mousedown', function (e) {
            curCol = e.target.parentElement; // current column
            nxtCol = curCol.nextElementSibling; // sibling column
            pageX = e.pageX; // x coord of mouse pointer
            var padding = paddingDiff(curCol);
            curColWidth = curCol.offsetWidth - padding;
            if (nxtCol)
                nxtColWidth = nxtCol.offsetWidth - padding;
        });
        // mouseover event: creates a line to indicate that it can be resized
        div.addEventListener('mouseover', function (e) {
            e.target.style.borderRight = '1.5px dashed #000000';
        });
        // mouseout event: removes line
        div.addEventListener('mouseout', function (e) {
            e.target.style.borderRight = '';
        });
        // mousemove event
        document.addEventListener('mousemove', function (e) {
            resizeColumnResizers(); // Resize all of the column resizer divs to be the same size as the table
            if (curCol) {
                var diffX = e.pageX - pageX;
                if (nxtCol)
                    nxtCol.style.width = (nxtColWidth - (diffX)) + 'px'; // set the new sibling column width
                curCol.style.width = (curColWidth + diffX) + 'px'; // set the new current column width
            }
        });
        // mouseup event: clear all values
        document.addEventListener('mouseup', function (e) {
            curCol = undefined;
            nxtCol = undefined;
            pageX = undefined;
            nxtColWidth = undefined;
            curColWidth = undefined;
        });
    }
    // Creates a div which can be interacted with to change the column size
    function createDiv(height) {
        var div = document.createElement('div');
        div.setAttribute('class', 'column-resizer-div');
        div.style.top = 0;
        div.style.right = 0;
        div.style.width = '5px';
        div.style.position = 'absolute';
        div.style.cursor = 'col-resize';
        div.style.userSelect = 'none';
        div.style.height = height - 5 + 'px';
        return div;
    }
    function paddingDiff(col) {
        if (getStyleVal(col, 'box-sizing') == 'border-box') {
            return 0;
        }
        var padLeft = getStyleVal(col, 'padding-left');
        var padRight = getStyleVal(col, 'padding-right');
        return (parseInt(padLeft) + parseInt(padRight));
    }
    function getStyleVal(elm, css) {
        return (window.getComputedStyle(elm, null).getPropertyValue(css));
    }
    resizeableTable();
});

// Functionality for displaying how the table is being sorted
$(document).ready(function () {
    // Create a new div under the filter search div. Contains the info for how the table is sorted
    function createSortedByDiv() {
        var sortedByDiv = document.createElement('div');
        const textContent = document.createTextNode('Table sorted by ');
        const sortingInfo = document.createElement('span');
        sortingInfo.setAttribute('id', 'sortedBy');
        sortedByDiv.appendChild(textContent);
        sortedByDiv.appendChild(sortingInfo);
        sortedByDiv.setAttribute('style', 'padding: 10px;');
        const tableFilterDiv = document.getElementById('myTable_filter');
        tableFilterDiv.parentNode.insertBefore(sortedByDiv, tableFilterDiv.nextSibling);
    }
    // Update the text to display which column is currently being used to sort the table and in which order
    window.updateSortingInfo = function updateSortingInfo() {
        var table = document.getElementById('myTable');
        var tableHeaders = table.getElementsByTagName('tr')[0];
        var cols = tableHeaders.children;
        for (let i = 0; i < cols.length; i++) {
            let className = cols[i].getAttribute('class');
            let header = cols[i].innerHTML;
            if (className === 'sorting sorting_asc') {
                document.getElementById('sortedBy').innerHTML = `${header} in ascending order:`;
                break;
            }
            else if (className === 'sorting sorting_desc') {
                document.getElementById('sortedBy').innerHTML = `${header} in descending order:`;
                break;
            }
        }
    }
    // Add onclick events for each of the table headers to call updateSortingInfo()
    function addOnClickEvents() {
        var table = document.getElementById('myTable');
        var tableHeaders = table.getElementsByTagName('tr')[0];
        var cols = tableHeaders.children;
        for (let i = 0; i < cols.length; i++) {
            cols[i].setAttribute('onclick', 'updateSortingInfo();');
        }
    }
    // Initial calls for inital page setup
    createSortedByDiv();
    addOnClickEvents();
    updateSortingInfo();
});
