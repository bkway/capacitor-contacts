import XCTest
import Capacitor
@testable import Plugin

class PluginTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testFetch() {
        // This is an example of a functional test case for a plugin.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
        
        let query = "Apple"
        let plugin = CapacitorContacts()
        
        let call = CAPPluginCall(callbackId: "test", options: [
            "query": query
        ], success: { (result, call) in
            let resultValue = result!.data["value"]
            XCTAssertNotNil(resultValue)
        }, error: { (err) in
            XCTFail(err!.message)
        })
        
        plugin.fetch(call!)
    }
}
