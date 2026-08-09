#!/bin/bash
set -eu

until mongosh --host mongo-archery:27017 --quiet --eval "quit(db.adminCommand('ping').ok ? 0 : 1)"; do
  sleep 2
done

until mongosh --host mongo-archery-secondary:27017 --quiet --eval "quit(db.adminCommand('ping').ok ? 0 : 1)"; do
  sleep 2
done

mongosh --host mongo-archery:27017 --quiet --eval '
try {
  void rs.status();
} catch (error) {
  if (error.codeName !== "NotYetInitialized") {
    throw error;
  }

  const result = rs.initiate({
    _id: "rs0",
    members: [
      { _id: 0, host: "mongo-archery:27017", priority: 2 },
      { _id: 1, host: "mongo-archery-secondary:27017", priority: 1 }
    ]
  });

  if (!result.ok) {
    throw new Error("Replica Set initialization failed");
  }
}
'

until mongosh --host mongo-archery:27017 --quiet --eval '
  const states = rs.status().members.map(member => member.stateStr);
  quit(states.includes("PRIMARY") && states.includes("SECONDARY") ? 0 : 1);
'; do
  sleep 2
done


mongosh \
  "mongodb://mongo-archery:27017,mongo-archery-secondary:27017/archerydb?replicaSet=rs0" \
  /scripts/configs/01-SchemaValidation.js

mongosh \
  "mongodb://mongo-archery:27017,mongo-archery-secondary:27017/archerydb?replicaSet=rs0" \
  /scripts/configs/02-Indexes.js