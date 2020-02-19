require 'httparty'

TEST_DATA_DIR = "data"
BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjcGNkcy1zZXJ2ZXItcmkiLCJwYXRpZW50IjoiYWRtaW4iLCJpYXQiOjE1ODIwNTQxMzV9.nUZKr9WUMXPG2v3iDSbz03fcZJm41nUHIiNmL80PJh0"
FHIR_SERVER = 'http://localhost:8080/cpcds-server/fhir/'
DEBUG = false # Set to true to only upload first bundle
$count = 0


def upload_test_patient_data(server)
    file_path = File.join(__dir__, TEST_DATA_DIR, '*.json')
    filenames = Dir.glob(file_path)
    filenames.each do |filename|
        bundle = JSON.parse(File.read(filename), symbolize_names: true)
        upload_bundle(bundle, server)
        break if DEBUG
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