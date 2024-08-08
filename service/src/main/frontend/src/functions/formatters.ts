/* eslint-disable no-var */
import { Ref, WritableComputedRef, computed, defineComponent, ref } from 'vue';

// Parses a Value to remove uncessessary 'undefined' or empty values.
export function parseVal(colName: any, colValue: any): string {
  if (colName === 'Types' || colName === 'Descriptions') {
    if (colValue == undefined) {
      return '';
    } else {
      return colValue.toString();
    }
  } else {
    return colValue;
  }
}

// Produces the max substring for the table, ads '...' if above 34 chars.
export function maxSubstring(str: any): any {
  if (str == undefined) {
    return;
  } else if (str.length > 34) {
    return str.substring(0, 32) + ' ...';
  } else {
    return str;
  }
}

// Defines how the expandability is parsed on the table.
export function buttonParse(col: any, row: any): boolean {
  return row.button == 1;
}

// Toggles how the row collapses based on the DOM. Filters visible rows.
export function toggleVisibility(row: any) {
  row.toggleVisibility();
}

// Set the Visibility in DOM, sorts and filters by lastUpdated, and the respective row to render button.
export function setVisibility(rows: readonly any[]) {
  const fieldVisibility: Map<string, Ref<boolean>> = new Map<
    string,
    Ref<boolean>
  >();
  const buttonValues: Map<string, number> = new Map<string, number>();

  for (var row of rows) {
    let maxUp: number = row.lastUpdated;
    const fieldUp: any = row.fieldName;
    for (const scan of rows) {
      if (fieldUp === scan.fieldName && maxUp < scan.lastUpdated) {
        maxUp = scan.lastUpdated;
        buttonValues.set(fieldUp, maxUp);
      }
    }
  }

  for (var row of rows) {
    if (
      buttonValues.has(row.fieldName) &&
      row.lastUpdated == buttonValues.get(row.fieldName)
    ) {
      row['duplicate'] = 0;
      row['button'] = true;
    } else if (
      buttonValues.has(row.fieldName) &&
      row.lastUpdated != buttonValues.get(row.fieldName)
    ) {
      row['duplicate'] = 1;
      row['button'] = false;
    } else {
      row['duplicate'] = 0;
      row['button'] = false;
    }

    const fieldName = row.fieldName;
    if (!fieldVisibility.has(fieldName)) {
      fieldVisibility.set(fieldName, ref<boolean>(false));
    }

    const visibility = fieldVisibility.get(fieldName);

    row['toggleVisibility'] = () => {
      visibility!.value = !visibility?.value;
    };
    row['isVisible'] = visibility;
  }

  return rows;
}

// Lets the DOM know what is visible and what is not based on setVisibility filters.
export function isVisible(row: any) {
  return row.duplicate == 0 || row.isVisible.value;
}
