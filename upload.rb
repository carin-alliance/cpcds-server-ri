require 'httparty'

TEST_DATA_DIR = "CPCDS_patient_data"
BEARER_TOKEN = "Bearer 39ff939jgg"
FHIR_SERVER = 'http://localhost:8080/cpcds-server/fhir/'
$count = 0


def upload_test_patient_data(server)
    file_path = File.join(__dir__, TEST_DATA_DIR, '*.json')
    filenames = Dir.glob(file_path)
    filenames.each do |filename|
        bundle = JSON.parse(File.read(filename), symbolize_names: true)
        upload_bundle(bundle, server)
    end
end

def upload_bundle(bundle, server)
    puts "Uploading bundle #{$count + 1}..."
    begin
        response = HTTParty.post(server, 
            body: bundle.to_json, 
            headers: { 'Content-Type': 'application/json', 'Authorization': BEARER_TOKEN }
        )
        rescue StandardError
    end
    $count += 1
    puts "  ... uploaded bundle #{$count}"
end

if ARGV.length == 0
    server = FHIR_SERVER
else
    server = ARGV[0]
end

puts "POSTING patient bundles to #{server}"
upload_test_patient_data(server)
puts "Uploaded #{$count} patient bundles to #{server}"