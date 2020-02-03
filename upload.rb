require 'httparty'

TEST_DATA_DIR = "CPCDS_patient_data"
FHIR_SERVER = 'http://localhost:8080/cpcds-server/fhir/'
SUPPORTED_RESOURCE_TYPES = ['Patient', 'Claim', 'ExplanationOfBenefit']
$count = 0


def upload_test_patient_data(server)
    file_path = File.join(__dir__, TEST_DATA_DIR, '*.json')
    filenames = Dir.glob(file_path)
    filenames.each do |filename|
        bundle = JSON.parse(File.read(filename), symbolize_names: true)
        bundle[:entry].each do |entry|
            upload_resource(entry[:resource], server)
        end
    end
end

def upload_resource(resource, server)
    resource_type = resource[:resourceType]
    if SUPPORTED_RESOURCE_TYPES.include? resource_type
        id = resource[:id]
        begin
            HTTParty.put(
                "#{server}/#{resource_type}/#{id}",
                body: resource.to_json,
                headers: { 'Content-Type': 'application/json' }
            )
            rescue StandardError
        end
        puts "#{resource_type}/#{id}"
        $count += 1
    end
end

if ARGV.length == 0
    server = FHIR_SERVER
else
    server = ARGV[0]
end

puts "POSTING resources to #{server}"
upload_test_patient_data(server)
puts "Uploaded #{$count} resources to #{server}"