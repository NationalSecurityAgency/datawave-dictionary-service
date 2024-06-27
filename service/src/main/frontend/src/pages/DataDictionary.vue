<template>
  <main class="main col" style="height: 100vh">
    <div
      class="row"
      style="
        width: 60%;
        height: 4%;
        justify-content: center;
        align-self: center;
      "
    >
      <label class="title">Data Dictionary</label>
    </div>
    <div class="row" style="width: 100%; height: 85%">
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
        style="font-size: smaller; height: 100%; width: 100%"
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
          <q-tr
            :class="props.row.button == 0 ? 'bg-grey-2 text-black' : ''"
            :props="props"
            v-if="Formatters.isVisible(props.row)"
          >
            <q-td style="width: 60px; min-width: 60px">
              <q-btn
                size="sm"
                color="blue"
                round
                dense
                @click="
                  {
                    props.expand = !props.expand;
                    Formatters.toggleVisibility(props.row);
                  }
                "
                :icon="props.expand ? 'remove' : 'add'"
                v-if="Formatters.buttonParse(props.cols, props.row)"
              />
            </q-td>
            <q-td
              v-for="col in props.cols"
              :key="col.name"
              :props="props"
              style="font-size: x-small"
              :title="Formatters.parseVal(col.name, col.value)"
            >
              {{
                Formatters.maxSubstring(
                  Formatters.parseVal(col.name, col.value)
                )
              }}
            </q-td>
          </q-tr>
        </template>
      </q-table>
    </div>
  </main>
</template>

<script setup lang="ts">
import { Ref, ref } from 'vue';
import { QTable, QTableProps, exportFile, useQuasar } from 'quasar';
import axios from 'axios';
import * as Formatters from '../functions/formatters';

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
const paginationFront = ref({
  rowsPerPage: 23,
  sortBy: 'fieldName',
});

// Confirms the table is full as long as the window's size changes.
window.onresize = () => {
  paginationFront.value.rowsPerPage = Math.floor(
    (table.value.$el.clientHeight - 33 - 52 - 28) / 28
  );
};

// AXIOS - Loads from REST endpoint.
axios
  .get(process.env.ENDPOINT!)
  .then((response) => {
    rows = response.data.MetadataFields;
    rows = Formatters.setVisibility(rows);
    paginationFront.value.rowsPerPage = Math.floor(
      (table.value.$el.clientHeight - 33 - 52 - 28) / 28
    );

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

// Attempts to Wrap the CSV and Download.
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
</script>
