declare module "@capacitor/core" {
  interface PluginRegistry {
    CapacitorContacts: CapacitorContactsPlugin;
  }
}

export interface CapacitorContactsPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
