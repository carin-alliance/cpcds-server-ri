require "zip"
require "tmpdir"
require "httparty"
require "fileutils"

TEST_DATA_DIR = "CPCDS_patient_data"
SAMPLE_DATA_DIR = "sample_data_2_0_0"
# FHIR_SERVER = "http://localhost:8080/cpcds-server/fhir"
FHIR_SERVER = "http://hapi.fhir.org/baseR4"
$count = 0
$retry_resources = Array.new()

def upload_test_patient_data(server)
  file_path = File.join(__dir__, TEST_DATA_DIR, "*.json")
  filenames = Dir.glob(file_path)
  puts "Uploading #{filenames.length} resources"
  filenames.each_with_index do |filename, index|
    bundle = JSON.parse(File.read(filename), symbolize_names: true)
    upload_bundle(bundle, server)
  end
end

def upload_carrinBBExtracts_data(server)
  file_path = File.join(__dir__, "carrinBBExtracts", "**/*.json")
  filenames =
    Dir.glob(file_path)
      .partition { |filename| filename.include? "List" }
      .flatten
  puts "Uploading #{filenames.length} resources"
  filenames.each_with_index do |filename, index|
    resource = JSON.parse(File.read(filename), symbolize_names: true)
    response = upload_resource(resource, server)
    # binding.pry unless response.success?
    if index % 100 == 0
      puts index
    end
  end
end

def upload_sample_data(server, data_dir)
  file_path = File.join(__dir__, data_dir, "*.json")
  filenames = Dir.glob(file_path)
  puts "Uploading #{filenames.length} resources"
  filenames.each_with_index do |filename, index|
    resource = JSON.parse(File.read(filename), symbolize_names: true)
    response = upload_resource(resource, server)
  end
end

def upload_out_data(server)
  file_path = File.join(__dir__, "orgs", "*.json")
  filenames =
    Dir.glob(file_path)
      .partition { |filename| filename.include? "List" }
      .flatten
  puts "Uploading #{filenames.length} resources"
  filenames.each_with_index do |filename, index|
    resource = JSON.parse(File.read(filename), symbolize_names: true)
    response = upload_resource(resource, server)
    # binding.pry unless response.success?
    if index % 100 == 0
      puts index
    end
  end
end

def upload_bundle(bundle, server)
  puts "Uploading bundle #{$count + 1}..."
  begin
    response = HTTParty.post(server,
                             body: bundle.to_json,
                             headers: { 'Content-Type': "application/json" })
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
    response = HTTParty.put(
      "#{server}/#{resource_type}/#{id}",
      body: resource.to_json,
      headers: { 'Content-Type': "application/json" },
    )
    if response.code != 201 && response.code != 200
      puts " ... ERROR: #{response.code}"
      $retry_resources.push(resource)
    else
      $count += 1
    end
  rescue StandardError
    puts " ... ERROR: Unable to upload resource. Make sure the server is accessible."
    $retry_resources.push(resource)
  end
end

def upload_ig_examples(server)
  # Update this specification download url with each IG version
  definitions_url = "https://build.fhir.org/ig/HL7/carin-bb/examples.json.zip"
  definitions_data = HTTParty.get(definitions_url, verify: false)
  definitions_file = Tempfile.new
  begin
    definitions_file.write(definitions_data)
  rescue => e
    puts "Something went wrong while downloading the IG specification: #{e.message}"
    return
  ensure
    definitions_file.close
  end

  Zip::File.open(definitions_file.path) do |zip_file|
    zip_file.entries
      .select { |entry| entry.name.end_with? ".json" }
      .reject { |entry| entry.name.start_with? "ImplementationGuide" }
      .each do |entry|
      resource = JSON.parse(entry.get_input_stream.read, symbolize_names: true)
      response = upload_resource(resource, server)
    end
  end
ensure
  definitions_file.unlink
end

def upload_retry_resources(server)
  resources = $retry_resources
  $retry_resources = Array.new()
  resources.each do |resource|
    upload_resource(resource, server)
  end
end

if ARGV.length == 0
  server = FHIR_SERVER
  data_dir = SAMPLE_DATA_DIR
elsif ARGV.length == 1
  server = FHIR_SERVER
  data_dir = ARGV[0]
else
  server = ARGV[1]
  data_dir = ARGV[0]
end

# puts "POSTING patient bundles to #{server}"
# upload_test_patient_data(server)
# puts "PUTTING #{data_dir} to #{server}"
# upload_sample_data(server, data_dir)
puts "PUTTING IG example resources to #{server}"
upload_ig_examples(server)
puts "Uploaded #{$count} resources to #{server}"
puts "Retyring #{$retry_resources.length} resources..."
upload_retry_resources(server)
puts "#{$retry_resources.length} still failed."

# puts "POSTING patient bundles to #{server}"
# upload_test_patient_data(server)
# puts "Uploaded #{$count} patient bundles to #{server}"

# puts "PUTTING carrinBBExtracts resources to #{server}"
# upload_carrinBBExtracts_data(server)
# puts "Uploaded resources to #{server}"

# puts "PUTTING out resources to #{server}"
# upload_out_data(server)
# upload_sample_data(server)
# puts "Uploaded resources to #{server}"

# puts "PUTTING IG example resources to #{server}"
# upload_ig_examples(server)
# puts "DONE"
