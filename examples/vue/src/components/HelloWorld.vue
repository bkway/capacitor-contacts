<template>
  <div class="hello">
    <p>
      <span @click="checker">Check Permissions</span>
      {{ permissions }}
    </p>
    <p>
          <span @click="requester">Request Permissions</span>
          {{ justonce }}
        </p>
    <p>
      <span @click="fetcher">Fetch Contacts</span>
      {{ fetched }}
    </p>
    <p>
      <span @click="picker">Pick Contact</span>
      {{ picked }}
    </p>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { Plugins } from '@capacitor/core';

@Component
export default class HelloWorld extends Vue {
  public permissions = '';

  public justonce = '';

  public fetched = 'waiting...';

  public picked = 'waiting...';

  public async checker() {
    this.permissions = (await Plugins.CapacitorContacts.hasPermissions()).allowed ? 'yes' : 'no';
  }

  public async requester() {
    this.justonce = (await Plugins.CapacitorContacts.requestPermissions()).allowed ? 'yes' : 'no';
  }

  public async picker() {
    this.picked = await Plugins.CapacitorContacts.pick({ fields: ['familyName', 'givenName', 'email', 'phone'] });
  }

  public async fetcher() {
    // const picked = JSON.stringify(Plugins.CapacitorContacts);
    this.fetched = await Plugins.CapacitorContacts.fetch({ fields: ['familyName', 'givenName', 'email', 'phone'], query: 'mac' });
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
