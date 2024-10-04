// Called by exportTable to format the CSV
// This is how Rows are Parsed and Formatted in the CSV
// This may be changed to customize or change if a user would like to change/add data in the CSV
export function wrapCsvValue(val?: any, formatFn?: any, row?: any) {
  let formatted = formatFn !== void 0 ? formatFn(val, row) : val;

  formatted =
    formatted === void 0 || formatted === null ? '' : String(formatted);

  formatted = formatted.split('"').join('""');
  return `"${formatted}"`;
}
