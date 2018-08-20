#!/usr/bin/python

import sys
import json
import struct

from confluent.schemaregistry.client import CachedSchemaRegistryClient
from confluent.schemaregistry.serializers import MessageSerializer, Util

HAS_FAST = False
try:
    from fastavro import schemaless_reader

    HAS_FAST = True
except ImportError:
    pass

client = CachedSchemaRegistryClient(url='http://94.130.90.122:8081')
serializer = MessageSerializer(client)

schema_id,avro_schema,schema_version = client.get_latest_schema('ErrorReport_Joined_test1-value')

def input_stream():
    """
        Consume STDIN and yield each record that is received from MemSQL
    """
    while True:
        byte_len = sys.stdin.read(8)
        if len(byte_len) == 8:
            byte_len = struct.unpack("L", byte_len)[0]
            result = sys.stdin.read(byte_len)
            yield result
        else:
            assert len(byte_len) == 0, byte_len
            return

def log(message):
    """
        Log an informational message to stderr which will show up in MemSQL in
        the event of transform failure.
    """
    sys.stderr.write(message + "\n")

def emit(message):
    """
        Emit a record back to MemSQL by writing it to STDOUT.  The record
        should be formatted as TSV or CSV as it will be parsed by LOAD DATA.
    """
    sys.stdout.write(message + "\n")

def datum_to_flat_fields(datum):
    return datum.values()

def fields_to_csv(fields):
    return ",".join([(json.dumps(f) if f is not None else '\N') for f in fields])

def extract_path(path, datum):
    r = datum
    for field in path:
        r = r[field]
        return r

def extract_paths(paths, datum):
    return [extract_path(p, datum) for p in paths]

paths = [['errortime'], ['errorreportseq'], ['errorid'], ['fromuserid'], ['fromuseruri'], ['fromuritypeid'],
  ['fromuritype'], ['touserid'], ['touseruri'], ['touritypeid'], ['touritype'], ['conferenceuriid'], ['conferenceuri'],
  ['conferencechecksum'], ['conferenceuritypeid'], ['conferenceuritype'], ['sessionidtime'], ['sessionidseq'], ['sourceid'],
  ['sourcefqdn'], ['applicationid'], ['applicationname'], ['msdiagheader'], ['clientversionid'], ['clientversion'],
  ['clientversiontype'], ['clientuaname'], ['clientuacategory'], ['iscapturedbyserver'], ['flag'], ['telemetryid'],
  ['sessionsetuptime'], ['serverid'], ['serverfqdn'], ['poolid'], ['poolfqdn'], ['tenantid']]

def decode(data):
    """
        Deserialize avro with schema ID coming from Kafka
    """
    return fields_to_csv(extract_paths(paths, serializer.decode_message(data)))


log("Begin transform")

# We start the transform here by reading from the input_stream() iterator.
for data in input_stream():
    decoded = decode(data)
    emit(decoded)

log("End transform")
