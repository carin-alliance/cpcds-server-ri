require 'zip'
require 'tmpdir'
require 'httparty'
require 'fileutils'

TEST_DATA_DIR = "CPCDS_patient_data"
FHIR_SERVER = 'http://localhost:8080/cpcds-server/fhir/'
$count = 0


def upload_test_patient_data(server)
    file_path = File.join(__dir__, TEST_DATA_DIR, '*.json')
    filenames = Dir.glob(file_path)
    puts "Uploading #{filenames.length} resources"
    filenames.each_with_index do |filename, index|
        bundle = JSON.parse(File.read(filename), symbolize_names: true)
        upload_bundle(bundle, server)
    end
end

def upload_bundle(bundle, server)
    puts "Uploading bundle #{$count + 1}..."
    begin
        response = HTTParty.post(server, 
            body: bundle.to_json, 
            headers: { 'Content-Type': 'application/json' }
        )
        rescue StandardError
    end
    $count += 1
    puts "  ... uploaded bundle #{$count}"
end

def upload_resource(resource, server)
    resource_type = resource[:resourceType]
    id = resource[:id]
    puts "Uploading #{resource_type}/#{id}"
    begin
        HTTParty.put(
        "#{server}/#{resource_type}/#{id}",
        body: resource.to_json,
        headers: { 'Content-Type': 'application/json' }
        )
    rescue StandardError
    end
end

def upload_ig_examples(server)
    definitions_url = 'https://build.fhir.org/ig/HL7/carin-bb/examples.json.zip'
    definitions_data = HTTParty.get(definitions_url, verify: false)
    definitions_file = Tempfile.new
    begin
        definitions_file.write(definitions_data)
    ensure
        definitions_file.close
    end

    Zip::File.open(definitions_file.path) do |zip_file|
        zip_file.entries
        .select { |entry| entry.name.end_with? '.json' }
        .reject { |entry| entry.name.start_with? 'ImplementationGuide' }
        .each do |entry|
            resource = JSON.parse(entry.get_input_stream.read, symbolize_names: true)
            response = upload_resource(resource, server)
        end
    end
ensure
    definitions_file.unlink
end

if ARGV.length == 0
    server = FHIR_SERVER
else
    server = ARGV[0]
end

puts "POSTING patient bundles to #{server}"
upload_test_patient_data(server)
puts "Uploaded #{$count} patient bundles to #{server}"
puts "PUTTING IG example resources to #{server}"
upload_ig_examples(server)
puts "DONE"