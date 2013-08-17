
set -u

CLASSPATH=/home/evans/NetBeansProjects/vellum/build/classes
for jar in /home/evans/NetBeansProjects/vellum/dist/lib/*.jar
do
  echo $jar
  CLASSPATH=$CLASSPATH:$jar
done

echo CLASSPATH=$CLASSPATH

set -x

secstore=~/tmp/dual.sec.jceks
secalias=dek-2013
privatestore=~/tmp/dual.private.jks
truststore=~/tmp/dual.public.jks
cert=~/tmp/dual.pem
pass=test1234

javaks() {
  java \
    -Djavax.net.ssl.keyStore=$privatestore \
    -Djavax.net.ssl.keyStorePassword=$pass \
    -Djavax.net.ssl.keyPassword=$pass \
    -Djavax.net.ssl.trustStore=$truststore \
    -Djavax.net.ssl.trustStorePassword=$pass \
    $@
}

nc3() {
    sleep 1 
    echo "evans:eeee" | nc -v localhost 4444
    echo "hentyw:hhhh" | nc -v localhost 4444
    echo "brandonh:bbb" | nc -v localhost 4444
}

nc2() {
    sleep 1 
    echo "evans:eeee" | nc -v localhost 4444
    echo "hentyw:hhhh" | nc -v localhost 4444
}

jc() {
  javaks dualcontrol.DualControlClient "$1"
}

jc1() {
    sleep 1 
    jc "evans:eeee" 
}

jc2() {
    sleep 1 
    jc "evans:eeee" 
    jc "hentyw:hhhh"
}

jc3() {
    sleep 1 
    jc "evans:eeee" 
    jc "hentyw:hhhh"
    jc "brandonh:bbbb"
}
 
initks() {
  dualalias="dualserver"
  dname="CN=server, OU=dualcontrol, O=test, L=ct, S=wp, C=za"
  rm -f $privatestore
  rm -f $truststore
  rm -f $secstore
  keytool -keystore $privatestore -storepass "$pass" -keypass "$pass" -alias "$dualalias" -genkeypair -dname "$dname"
  keytool -keystore $privatestore -storepass "$pass" -list | grep Entry
  keytool -keystore $privatestore -storepass "$pass" -keypass "$pass" -alias "$dualalias" -exportcert -rfc | 
    openssl x509 -text | grep "Subject:"
  keytool -keystore $privatestore -storepass "$pass" -keypass "$pass" -alias "$dualalias" -exportcert -rfc > $cert
  keytool -keystore $truststore -storepass "$pass" -alias "$dualalias" -importcert -noprompt -file $cert
  keytool -keystore $truststore -storepass "$pass" -list | grep Entry
}

command1_genseckey() {
  javaks -DDualControl.alias=$1 -DDualControl.inputs=3 dualcontrol.DualControlKeyTool \
     -keystore $secstore -storetype JCEKS -storepass $pass -genseckey -keyalg DESede
  keytool -keystore $secstore -storetype JCEKS -storepass $pass -list
}

command0_app() {
  javaks -DDualControl.inputs=2 dualcontrol.TestApp $secstore $pass $secalias
}

command0_keystoreserver() {
  javaks dualcontrol.FileServer 127.0.0.1 4445 1 1 127.0.0.1 $secstore
}

keystoreclient() {
  sleep 1
  javaks dualcontrol.FileClient 127.0.0.1 4445
}

command0_testkeystoreserver() {
  keystoreclient & command0_keystoreserver
  sleep 2
}

command0_cryptoserver() {
  javaks dualcontrol.CryptoServer 127.0.0.1 4446 1 2 127.0.0.1 $secstore $pass
}

cryptoclient() {
  sleep 1
  jc "evans:eeee" 
  jc "hentyw:hhhh"
  sleep 1
  data=`javaks dualcontrol.CryptoClient 127.0.0.1 4446 "$secalias:DESede/CBC/PKCS5Padding:ENCRYPT:pq7ZjIcIK9A=:111122223333444"`
  javaks dualcontrol.CryptoClient 127.0.0.1 4446 "$secalias:DESede/CBC/PKCS5Padding:DECRYPT:$data"
}

command0_testcryptoserver() {
  cryptoclient & command0_cryptoserver
  sleep 2
}

command0_testgenseckey() {
  initks 
  jc3 | command1_genseckey $secalias
  sleep 2
  if ! nc -z localhost 4444
  then
    jc2 | command0_app
    sleep 2
  fi
}

command0_client() {
  javaks dualcontrol.DualControlClient
}

command0_testgenseckey
command0_testkeystoreserver
command0_testcryptoserver
command0_client

#sh /home/evans/NetBeansProjects/svn/vellum/trunk/src/dualcontrol/dualtest.sh > /home/evans/NetBeansProjects/svn/vellum/trunk/src/dualcontrol/dualtest.out 2>&1

