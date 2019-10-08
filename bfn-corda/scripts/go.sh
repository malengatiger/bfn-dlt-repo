#!/usr/bin/env bash
# ----------- DEPLOY CORDA NODES
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " ........deploying Corda nodes
./scripts/deploy.sh

echo -------- ️ "🍀 🍀 🍀 🍀 🍀 🍀 🍀 " done deploying Corda nodes
# ------------ NOTARY NODE
echo 🕗  Sleeping for 10 seconds
sleep 10s # Waits 10 seconds.
echo \nWoke up, opening terminal for Notary Corda Node
ttab ./scripts/nnotary.sh

# ------------ REGULATOR NODE
echo 🕗 Sleeping for 10 seconds
sleep 10s # Waits 10 seconds.
echo Woke up, opening terminal for Regulator Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 Regulator
ttab ./scripts/nregulator.sh


# ------------ oct NODE
echo 🕗  Sleeping for 10 seconds
sleep 10s # Waits 10 seconds.
echo 🔆 Woke up, 🔆 🔆 🔆  opening terminal for OCT Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 OCT
ttab ./scripts/noct.sh


# ------------ cape NODE
echo 🕗 Sleeping for 10 seconds
sleep 10s # Waits 10 seconds.
echo 🔆 Woke up, 🔆 🔆 🔆  opening terminal for CAPE TOWN Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 CAPE TOWN
ttab ./scripts/ncape.sh
sleep 10s


# ------------ london NODE
echo 🕗  Sleeping for 10 seconds ........
sleep 10s # Waits 10 seconds.
echo 🔆 Woke up, 🔆 🔆 🔆 opening terminal for LONDON Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 LONDON
ttab ./scripts/nl.sh

# ------------ newyork NODE
echo 🕗  Sleeping for 10 seconds ........
sleep 10s # Waits 10 seconds.
echo 🔆 Woke up, 🔆 🔆 🔆 opening terminal for NEW YORK Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 NEW YORK
ttab ./scripts/ny.sh

echo 🔵 SLEEPING 🍎 30 🍎 seconds to let nodes finish booting up 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵
sleep 30s

echo 🔆 Woke up, 🧩 🧩 🧩 🧩 opening Regulator webserver
ttab ./scripts/wregulator.sh
sleep 10s

echo 🔆 Woke up, 🧩 🧩 🧩 🧩  opening OCT webserver
ttab ./scripts/woct.sh
sleep 10s

echo 🔆 Woke up, 🧩 🧩 🧩 🧩  opening CAPE TOWN webserver
ttab ./scripts/wcape.sh

sleep 10s
echo 🔆 Woke up, 🧩 🧩 🧩 🧩  opening LONDON webserver
ttab ./scripts/wlon.sh

sleep 10s
echo 🔆 Woke up, 🧩 🧩 🧩 🧩  opening NEW YORK webserver
ttab ./scripts/wyork.sh

echo -------- ️ "🍀 🍀 🍀 🍀 🍀 🍀 🍀 " done deploying Corda nodes and associated webservers



