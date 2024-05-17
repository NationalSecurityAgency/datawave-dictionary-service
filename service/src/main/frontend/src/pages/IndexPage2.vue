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
      <q-input
        class="input"
        v-model="input"
        outlined
        clearable
        dense
        color="black"
        label="Search"
      />
    </div>
    <q-table :rows="data" :column="columns"></q-table>
  </main>
</template>

<script lang="ts">
import axios from 'axios';
import { defineComponent } from 'vue';
//import { ref } from 'vue';

export interface DictionaryElements {
  Descriptions: string[];
  Types: string[];
  dataType: string;
  fieldName: string;
  forwardIndexed: boolean;
  indexOnly: boolean;
  internalFieldName: string;
  lastUpdated: string;
  normalized: boolean;
  reverseIndexed: boolean;
  tokenized: boolean;
}

export interface DictionaryHolder {
  dictVal: Array<DictionaryElements>;
}

export default defineComponent({
  name: 'IndexPage',

  data() {
    return {
      data: [],
      columns: [
        {
          label: 'Field Name',
          name: 'fieldName',
          field: 'fieldName',
          align: 'center',
        },
        {
          label: 'Internal FieldName',
          name: 'internalFieldName',
          field: 'internalFieldName',
          align: 'center',
        },
        {
          label: 'Data Type',
          name: 'dataType',
          field: 'dataType',
          align: 'center',
        },
        {
          label: 'Index Only',
          name: 'indexOnly',
          field: 'indexOnly',
          align: 'center',
        },
        {
          label: 'Forward Index',
          name: 'forwardIndexed',
          field: 'forwardIndexed',
          align: 'center',
        },
        {
          label: 'Reverse Index',
          name: 'revereseIndexed',
          field: 'fowardIndexed',
          align: 'center',
        },
        {
          label: 'Normalized',
          name: 'normalized',
          field: 'normalized',
          align: 'center',
        },
        { label: 'Types', name: 'Types', field: 'Types', align: 'center' },
        {
          label: 'Tokenized',
          name: 'tokenized',
          field: 'tokenized',
          align: 'center',
        },
        {
          label: 'Description',
          name: 'Descriptions',
          field: 'Descriptions',
          align: 'center',
        },
        {
          label: 'Last Updated',
          name: 'lastUpdated',
          field: 'lastUpdated',
          align: 'center',
        },
      ],
      input: '',
    };
  },

  methods: {
    async grabData(port: string): Promise<void> {
      await axios
        .get(`${port}/dictionary/data/v1/`)
        .then((response) => {
          console.log(response.data.MetadataFields);
          this.data = response.data.MetadataFields;
        })
        .catch((e) => console.log('Something happened?', e));
    },
  },
  beforeMount() {
    this.grabData('https://localhost:8643');
  },
});
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

.input {
  display: flex;
  align-items: center;
  height: 8px;
}
</style>
