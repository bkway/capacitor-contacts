import { WebPlugin } from '@capacitor/core';
import { CapacitorContactsPlugin } from './definitions';

export class CapacitorContactsWeb extends WebPlugin implements CapacitorContactsPlugin {
  constructor() {
    super({
      name: 'CapacitorContacts',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const CapacitorContacts = new CapacitorContactsWeb();

export { CapacitorContacts };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(CapacitorContacts);
