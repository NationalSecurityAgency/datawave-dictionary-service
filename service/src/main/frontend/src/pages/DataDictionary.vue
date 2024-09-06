<template>
  <div class="banner" style="color: #ffffff;"
    >PLACEHOLDER - BANNER</div>
  <main class="main col" style="height: 100vh">
    <div
      class="row"
      style="
        width: 60%;
        height: 4%;
        justify-content: center;
        align-self: center;
        font-size: 13px;"
    >
      <label class="title">Data Dictionary</label>
      <q-img
      :src="'icons/favicon-32x32.png'"
      spinner-color="white"
      style="height: 35px; max-width: 35px; margin-top: 2px;"
    />
    </div>
    <div class="row" style="width: 100%; height: 80%">
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
        dense
        style="font-size: smaller; height: 100%; width: 100%;"
        class="datawave-dicitonary-sticky-sass dark"
        :rows-per-page-options="[]"
      >
        <template v-slot:top-left>
          <q-btn
            style="margin-right: 2px;"
            size="12px"
            color="red"
            icon-right="archive"
            label="Export"
            no-caps
            @click="exportTable"
          />
          <q-btn
            style="margin-left: 2px;"
            size="12px"
            padding="5px 5px"
            color="red"
            :icon="isDark ? 'bi-sun-fill' : 'bi-moon-fill'"
            no-caps
            @click="toggleDark(); $q.dark.toggle();"
          />
        </template>
        <template v-slot:top-right>
          <q-input
            borderless
            dense
            debounce="300"
            v-model="changeFilter"
            placeholder="Search"
            @keydown.enter.prevent="queryTable"
            style="margin-right: 1em;"
          >
          </q-input>
          <q-btn
                size="12px"
                color="red"
                icon="search"
                round
                dense
                @click="queryTable"
              />
        </template>

        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th />
            <q-th style="font-size: 13.7px;" v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.label }}
            </q-th>
          </q-tr>
        </template>

        <template v-slot:body="props">
          <q-tr
            :props="props"
            v-if="Formatters.isVisible(props.row)"
          >
            <q-td style="width: 60px; min-width: 60px">
              <q-btn
                size="9px"
                color="red"
                round
                dense
                @click="
                  {
                    props.expand = !props.expand;
                    Formatters.toggleVisibility(props.row);
                    console.log(props)
                  }
                "
                :icon="props.row.isVisible.value ? 'remove' : 'add'"
                v-if="Formatters.buttonParse(props.cols, props.row)"
              />
              <q-icon
                  style="margin-left: 4px;"
                  size="1rem"
                  :name="'bi-arrow-right'"
                  color="red-5"
                  v-if="props.row.duplicate == 1"
              />
            </q-td>
            <q-td
              v-for="col in props.cols"
              :key="col.name"
              :props="props"
              style="font-size: 13px"
              :title="Formatters.parseVal(col.name, col.value)"
            >
              {{
                Formatters.maxSubstring(
                  Formatters.parseVal(col.name, col.value), col.name
                )
              }}
            </q-td>
          </q-tr>
        </template>
      </q-table>
    </div>
  </main>
  <div class="banner" style="color: #ffffff;"
  >PLACEHOLDER - BANNER</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { QTable, QTableProps, exportFile, useQuasar } from 'quasar';
import axios from 'axios';
import * as Formatters from '../functions/formatters';
import { useToggle, useDark } from '@vueuse/core';

// Defines Rows and Columns for the Table.
let rows: QTableProps['rows'] = [];
const columns: QTableProps['columns'] = [
  {
    label: 'Field Name',
    name: 'fieldName',
    field: 'fieldName',
    align: 'left',
    sortable: true,
    style: 'max-width: 275px; min-width: 275px',
  },
  {
    label: 'Internal FieldName',
    name: 'internalFieldName',
    field: 'internalFieldName',
    align: 'left',
    sortable: true,
    style: 'max-width: 275px; min-width: 275px',
  },
  {
    label: 'Data Type',
    name: 'dataType',
    field: 'dataType',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Index Only',
    name: 'indexOnly',
    field: 'indexOnly',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Forward Index',
    name: 'forwardIndexed',
    field: 'forwardIndexed',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Reverse Index',
    name: 'reverseIndexed',
    field: 'reverseIndexed',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Normalized',
    name: 'normalized',
    field: 'normalized',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Types',
    name: 'Types',
    field: 'Types',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Tokenized',
    name: 'tokenized',
    field: 'tokenized',
    align: 'left',
    sortable: true,
    style: 'max-width: 100px; min-width: 100px',
  },
  {
    label: 'Description',
    name: 'Descriptions',
    field: 'Descriptions',
    align: 'center',
    sortable: true,
    style: 'max-width: 200px; min-width: 200px',
  },
  {
    label: 'Last Updated',
    name: 'lastUpdated',
    field: 'lastUpdated',
    align: 'left',
    sortable: true,
    style: 'max-width: 75px; min-width: 75px',
  },
];

// Defines the Table References, loading for axios, search filter, and pagination to sort.
const table = ref();
const loading = ref(true);
const filter = ref('');
const changeFilter = ref('');
let paginationFront = ref({
  rowsPerPage: 30,
  sortBy: 'fieldName',
});

// AXIOS - Loads from REST endpoint.
axios
  .get(process.env.ENDPOINT!)
  .then((response) => {
    rows = response.data.MetadataFields;
    rows = Formatters.setVisibility(rows);
    console.log(rows)
    loading.value = false;
  })
  .catch((reason) => {
    console.log('Something went wrong? ' + reason);
  });

// Used to to export Quasar Data to a CSV and referenced in wrapCsvValue and exportTable.
const $q = useQuasar();

// Called by exportTable to format the CSV
function wrapCsvValue(val?: any, formatFn?: any, row?: any) {
  let formatted = formatFn !== void 0 ? formatFn(val, row) : val;

  formatted =
    formatted === void 0 || formatted === null ? '' : String(formatted);

  formatted = formatted.split('"').join('""');
  return `"${formatted}"`;
}

// Attempts to Wrap the CSV and Download
function exportTable(this: any) {
  const rowsToExport = table.value?.filteredSortedRows.filter(
    Formatters.isVisible
  );

  const content = [columns!.map((col) => wrapCsvValue(col.label))]
    .concat(
      rowsToExport.map((row: any) =>
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

// Runs through a Query Search Process
async function queryTable(this: any) {
  // Wait Until User Enters...
  await waitUp();

  // 1 - Filter the Rows
  const rowsToExport = table.value?.filteredSortedRows.filter(() => true);
  console.log(rowsToExport)

  // 2 - Define Refresh Trigger (By Pagination) and Orginial Rows Stored
  const originalRows = rows;
  const triggerRefresh = paginationFront.value.rowsPerPage;

  // 3 - Set the Current Rows to Filtered Value
  rows = Formatters.setVisibility(rowsToExport);

  // 4 - Trigger the Refresh
  paginationFront.value.rowsPerPage = 100;
  paginationFront.value.rowsPerPage = triggerRefresh;

  // 5 - Restore Original Rows for Next Query
  rows = originalRows;
}

// Waits for the User to Finish Typing Query
function waitUp() {
  filter.value = changeFilter.value;
}

const isDark = useDark();
const toggleDark = useToggle(isDark);

if (isDark.value) {
  $q.dark.set(true);
} else {
  $q.dark.set(false);
}

</script>

<style lang="sass">
.datawave-dicitonary-sticky-sass
  /* height or max-height is important */
  height: 310px

  thead tr:first-child th
    /* bg color is important for th; just specify one */
    backdrop-filter: blur(2.5px)


  thead tr th
    position: sticky
    z-index: 1
  thead tr:first-child th
    top: 0

  /* this is when the loading indicator appears */
  &.q-table--loading thead tr:last-child th
    /* height of all previous header rows */
    top: 48px

  /* prevent scrolling behind sticky top row on focus */
  tbody
    /* height of all previous header rows */
    scroll-margin-top: 48px
</style>
