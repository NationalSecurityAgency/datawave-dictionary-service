<template>
  <main class="main">
    <div class="header">
      <label class="title">Data Dictionary</label>
      <p class="information">
        When a value is present in the forward index types, this means that a
        field is indexed and informs you how your query terms will be treated
        (e.g. text, number, IPv4 address, etc). The same applies for the reverse
        index types with the caveat that you can also query these fields using
        leading wildcards. Fields that are marked as 'Index only' will not
        appear in a result set unless explicitly queried on. Index only fields
        are typically composite fields, derived from actual data, created by the
        software to make querying easier.
      </p>
      <q-table
        ref="table"
        :loading="loading"
        :rows="rows"
        :columns="columns"
        :filter="filter"
        v-model:pagination="paginationFront"
        row-key="fieldName"
      >
        <template v-slot:top-left>
          <q-btn
            color="primary"
            icon-right="archive"
            label="Export to csv"
            no-caps
            @click="exportTable"
          />
        </template>
        <template v-slot:top-right>
          <q-input
            borderless
            dense
            debounce="300"
            v-model="filter"
            placeholder="Search"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>

        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th />
            <q-th v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.label }}
            </q-th>
          </q-tr>
        </template>

        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td>
              <q-btn
                size="sm"
                color="accent"
                round
                dense
                @click="props.expand = !props.expand"
                :icon="props.expand ? 'remove' : 'add'"
              />
            </q-td>
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ parseVal(col.name, col.value) }}
            </q-td>
          </q-tr>
          <q-tr v-show="props.expand" :props="props">
            <q-td colspan="100%">
              <div
                class="text-left"
                v-for="vals in duplicateAry"
                :key="vals.fieldName"
              >
                <div v-if="vals.fieldName === props.row.fieldName" class="row">
                  <div class="col">{{ vals.fieldName }}</div>
                  <div class="col">
                    {{ vals.internalFieldName }}
                  </div>
                  <div class="col">data {{ vals.dataType }}</div>
                  <div class="col">index {{ vals.indexOnly }}</div>
                  <div class="col">foward {{ vals.forwardIndexed }}</div>
                  <div class="col">rev {{ vals.revereseIndexed }}</div>
                  <div class="col">norm {{ vals.normalized }}</div>
                  <div class="col">types {{ vals.types }}</div>
                  <div class="col">token {{ vals.tokenized }}</div>
                  <div class="col">desc {{ vals.Description }}</div>
                  <div class="col">update {{ vals.lastUpdated }}</div>

                  <!--FIX THIS FOR EACH VAL-->
                </div>
              </div>
            </q-td>
          </q-tr>
        </template>
      </q-table>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { QTable, QTableProps, exportFile, useQuasar } from 'quasar';
import axios from 'axios';

function parseVal(colName: any, colValue: any): string {
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

// const filterMethod = (
//   rows: readonly any[],
//   terms: any,
//   cols: readonly any[]
// ) => {
//   console.log('rows', rows);
//   rows = rows.filter((row) => {
//     if (row.fieldName === 'BAR_FIELD') {
//       return true;
//     } else {
//       return false;
//     }
//   });
//   return rows;
// };
// let filterArr: any = [];

let newAry: any = ref([]);
let strAry: any = ref([]);
let updateAry: any = ref([]);
let duplicateAry: any = ref([]);

const filterMethod = (rows: readonly any[]) => {
  rows = rows.filter((row) => {
    if (!strAry.value.includes(row.fieldName)) {
      strAry.value.push(row.fieldName);
      updateAry.value.push(row.lastUpdated);
      newAry.value.push(row);
      return true;
    } else {
      duplicateAry.value.push(row);
      return false;
    }
  });
  //console.log('before d', duplicateAry.value);
  //console.log('before n', newAry.value);
  //console.log(newAry.value);
  //return newAry;
  return findRecentlyUpdated();
};

const findRecentlyUpdated = () => {
  for (let i = 0; i < duplicateAry.value.length; i++) {
    var index = strAry.value.indexOf(duplicateAry.value[i].fieldName);
    if (newAry.value[index].lastUpdated < duplicateAry.value[i].lastUpdated) {
      //console.log('newAry', newAry.value[index].lastUpdated);
      let temp = newAry.value[index];
      newAry.value[index] = duplicateAry.value[i];
      duplicateAry.value[i] = temp;
    }
  }
  //console.log('after d', duplicateAry.value[0].lastUpdated);
  if (duplicateAry.value[0].lastUpdated === '1970010100000') {
    console.log('yes');
  }
  //console.log('after d', newAry.value);

  return sortDupArr();
};

const sortDupArr = () => {
  var i, j, temp;
  var swapped;
  for (i = 0; i < duplicateAry.value.length - 1; i++) {
    swapped = false;
    for (j = 0; j < duplicateAry.value.length - i - 1; j++) {
      if (
        duplicateAry.value[j].lastUpdated <
        duplicateAry.value[j + 1].lastUpdated
      ) {
        // Swap arr[j] and arr[j+1]
        temp = duplicateAry.value[j];
        duplicateAry.value[j] = duplicateAry.value[j + 1];
        duplicateAry.value[j + 1] = temp;
        swapped = true;
      }
    }

    // IF no two elements were
    // swapped by inner loop, then break
    if (swapped == false) break;
  }
  console.log('sort', duplicateAry.value);
  return newAry;
};

const maxSubstring = (str: any) => {
  console.log('str' + str);
  if (str == undefined) {
    return;
  } else if (str.length > 19) {
    return str.substring(0, 17) + '...';
  } else {
    var whitespace = '';
    var diff = 19 - str.length + 1;
    for (var x = 0; x < diff; x++) {
      whitespace = whitespace.concat('.');
    }
    return str + whitespace;
  }
};

const table = ref();

const columns: QTableProps['columns'] = [
  {
    label: 'Field Name',
    name: 'fieldName',
    field: 'fieldName',
    align: 'left',
    sortable: true,
    format(val, row) {
      return maxSubstring(val);
    },
  },
  {
    label: 'Internal FieldName',
    name: 'internalFieldName',
    field: 'internalFieldName',
    align: 'left',
    sortable: true,
    format(val, row) {
      return maxSubstring(val);
    },
  },
  {
    label: 'Data Type',
    name: 'dataType',
    field: 'dataType',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Index Only',
    name: 'indexOnly',
    field: 'indexOnly',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Forward Index',
    name: 'forwardIndexed',
    field: 'forwardIndexed',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Reverse Index',
    name: 'reverseIndexed',
    field: 'reverseIndexed',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Normalized',
    name: 'normalized',
    field: 'normalized',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Types',
    name: 'Types',
    field: 'Types',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Tokenized',
    name: 'tokenized',
    field: 'tokenized',
    align: 'left',
    sortable: true,
  },
  {
    label: 'Description',
    name: 'Descriptions',
    field: 'Descriptions',
    align: 'left',
    sortable: true,
    format(val, row) {
      return maxSubstring(val);
    },
  },
  {
    label: 'Last Updated',
    name: 'lastUpdated',
    field: 'lastUpdated',
    align: 'left',
    sortable: true,
  },
];

const loading = ref(true);
const filter = ref('');
let rows: QTableProps['rows'] = [];

// define pagination for standard table and table collapse
const paginationFront = ref({
  rowsPerPage: 23,
  sortBy: 'fieldName',
});

// load the data from the dictionary
axios
  .get('https://localhost:8643/dictionary/data/v1/')
  .then((response) => {
    rows = response.data.MetadataFields;
    rows = filterMethod(rows); //filter

    loading.value = false;
  })
  .catch((reason) => {
    console.log('Something went wrong? ' + reason);
  });

// function to export the table to a csv -> Default Functionality and can be modified
const $q = useQuasar();

function wrapCsvValue(val?: any, formatFn?: any, row?: any) {
  let formatted = formatFn !== void 0 ? formatFn(val, row) : val;

  formatted =
    formatted === void 0 || formatted === null ? '' : String(formatted);

  formatted = formatted.split('"').join('""');
  /**
   * Excel accepts \n and \r in strings, but some other CSV parsers do not
   * Uncomment the next two lines to escape new lines
   */
  // .split('\n').join('\\n')
  // .split('\r').join('\\r')

  return `"${formatted}"`;
}

function exportTable(this: any) {
  // naive encoding to csv format
  const content = [columns!.map((col) => wrapCsvValue(col.label))]
    .concat(
      table.value?.filteredSortedRows.map((row: any) =>
        columns!
          .map((col: any) =>
            wrapCsvValue(
              typeof col.field === 'function'
                ? col.field(row)
                : row[col.field === void 0 ? col.name : col.field],
              col.format,
              row
            )
          )
          .join(',')
      )
    )
    .join('\r\n');

  const status = exportFile('table-export.csv', content, 'text/csv');

  if (status !== true) {
    $q.notify({
      message: 'Browser denied file download...',
      color: 'negative',
      icon: 'warning',
    });
  }
}
</script>

<style scoped>
.main {
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
  margin-left: 5%;
  margin-right: 5%;
}

.header {
  font-family: 'Courier New', Courier, monospace;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  margin-bottom: 5em;
}

.title {
  font-size: large;
  margin-bottom: 0.5em;
}

.information {
  font-size: 10px;
  text-align: center;
}
</style>
