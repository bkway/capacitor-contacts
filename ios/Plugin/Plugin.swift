import Foundation
import Capacitor

import Contacts
import ContactsUI

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(CapacitorContacts)
public class CapacitorContacts: CAPPlugin, CNContactPickerDelegate {
    
    var call: CAPPluginCall?
    
    /**
     * Request access permissions from the user
     */
    @objc
    func requestAccess(_ call: CAPPluginCall) {
        
    }
    
    /**
     * Check status of the permissions
     */
    @objc
    func authorizationStatus(_ call: CAPPluginCall) {
        
    }
    
    /**
     * Fetch from contacts using filters
     */
    @objc
    func fetch(_ call: CAPPluginCall) {
        CAPLog.print("fetch called: " + call.callbackId)
        do {
            let store = CNContactStore()
            CAPLog.print("store grabbed")
            
            let fields = requestedFields(call)
            let req = CNContactFetchRequest.init(keysToFetch: fields)
// TODO have user specify which fields to search?
//            guard let matches = call.getArray("match", String.self) else {
//                call.error("Must provide an array of fields to match")
//                return
//            }
            CAPLog.print("request initialized")
            guard let query = call.getString("query")?.lowercased() else {
                call.error("Must provide a string to search")
                return
            }
            CAPLog.print("query built")
            var out: [[String: Any]] = []
            
            try store.enumerateContacts(with: req, usingBlock: { (contact, cursor) in
                CAPLog.print("enumerating: " + contact.identifier + " ...")
                var add = false
                if contact.areKeysAvailable([CNContactGivenNameKey as CNKeyDescriptor]) {
                    let found = contact.givenName.lowercased().contains(query)
                    if found { CAPLog.print("found in givenName: " + query + " = " + contact.givenName) }
                    add = found || add
                }
                if !add && contact.areKeysAvailable([CNContactFamilyNameKey as CNKeyDescriptor]) {
                    let found = contact.familyName.lowercased().contains(query)
                    if found { CAPLog.print("found in familyName: " + query + " = " + contact.familyName) }
                    add =  found || add
                }
                if !add && contact.areKeysAvailable([CNContactEmailAddressesKey as CNKeyDescriptor]) {
                    for email in contact.emailAddresses {
                        if email.value.lowercased.contains(query) {
                            CAPLog.print("found in email: " + query + " = " + (email.value as String))
                            add = true
                            break
                        }
                    }
                }
                if !add && contact.areKeysAvailable([CNContactPhoneNumbersKey as CNKeyDescriptor]) {
                    for phone in contact.phoneNumbers {
                        if phone.value.stringValue.contains(query) {
                            CAPLog.print("found in phone: " + query + " = " + (phone.value.stringValue))
                            add = true
                            break
                        }
                    }
                }
                CAPLog.print("done.")
                if add {
                    CAPLog.print("added!")
                    out.append(self.serialize(contact, fields: fields))
                }
            })
            CAPLog.print("fetch done. " + String(out.count))
            call.success(["contacts": out])
        } catch {
            call.error(error.localizedDescription)
        }
    }
    
    @objc
    func pick(_ call: CAPPluginCall) {
        CAPLog.print("starting pick...")
        DispatchQueue.main.async {
            self.call = call
            let format = "emailAddresses.@count > 0 || phoneNumbers.@count > 0"

            let picker = CNContactPickerViewController()
            picker.predicateForEnablingContact = NSPredicate(format: format)
            picker.predicateForSelectionOfContact = NSPredicate(value: true)
            picker.predicateForSelectionOfProperty = NSPredicate(value: true)
            
            picker.delegate = self
            self.bridge.viewController.present(picker, animated: true, completion: nil)
        }
    }
    
    @objc(contactPicker:didSelectContact:)
    public func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {
        CAPLog.print("contact picked: " + contact.identifier)
        let fields = self.requestedFields(self.call!)
        CAPLog.print(fields)
        if self.call != nil {
            let serialized = self.serialize(contact, fields: fields)
            call!.success(["value": serialized])
        } else {
            CAPLog.print("ContactPicker call abandoned.")
        }
        self.call = nil
//        self.selected = contact
    }
    
    @objc(contactPicker:didSelectContactProperty:)
    public func contactPicker(_ picker: CNContactPickerViewController, didSelect contactProperty: CNContactProperty) {
        CAPLog.print("contact property picked: " + (contactProperty.identifier ?? ""))
    }
    
    // TODO add missing fields
    @objc
    private func requestedFields(_ call: CAPPluginCall, defaults: [String] = []) -> [CNKeyDescriptor] {
    
        var fields: [String] = [CNContactIdentifierKey]
        
        for field in call.getArray("fields", String.self) ?? defaults {
            switch field {
            case "type":
                fields.append(CNContactTypeKey)
            case "givenName":
                fields.append(CNContactGivenNameKey)
            case "familyName":
                fields.append(CNContactFamilyNameKey)
            case "email":
                fields.append(CNContactEmailAddressesKey)
            case "phone":
                fields.append(CNContactPhoneNumbersKey)
            default:
                CAPLog.print("'" + field + "' field not recognized")
            }
        }
        return fields as [CNKeyDescriptor]
    }
    
    @objc
    private func serialize(_ contact: CNContact, fields: [CNKeyDescriptor]) -> [String: Any] {
        var out: [String: Any] = [:]
        for field in fields {
            CAPLog.print("Processing field: " + (field as! String) + " ...")
            switch field as! String {
            case CNContactGivenNameKey:
                CAPLog.print(contact.givenName)
                out["givenName"] = contact.givenName
            case CNContactFamilyNameKey:
                CAPLog.print(contact.familyName)
                out["familyName"] = contact.familyName
            case CNContactEmailAddressesKey:
                CAPLog.print(contact.emailAddresses)
                var emails: [[String:String]] = []
                for email in contact.emailAddresses {
                    let type = CNLabeledValue<NSString>.localizedString(forLabel: email.label ?? "")
                    emails.append(["type": type, "value": email.value as String])
                }
                out["email"] = emails
            case CNContactPhoneNumbersKey:
                CAPLog.print(contact.phoneNumbers)
                var phones: [[String:String]] = []
                for phone in contact.phoneNumbers {
                    let type = CNLabeledValue<CNPhoneNumber>.localizedString(forLabel: phone.label ?? "")
                    phones.append(["type": type, "value": phone.value.stringValue])
                }
                out["phone"] = phones
            default:
                CAPLog.print("skipping")
                // safe to skip
                continue
            }
        }
        return out
    }
}
