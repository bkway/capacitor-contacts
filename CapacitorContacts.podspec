
  Pod::Spec.new do |s|
    s.name = 'CapacitorContacts'
    s.version = '0.0.1'
    s.summary = 'Give access to device contacts'
    s.license = 'MIT'
    s.homepage = 'github.com/bkway/capacitor-contacts'
    s.author = 'TommyO'
    s.source = { :git => 'github.com/bkway/capacitor-contacts', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end