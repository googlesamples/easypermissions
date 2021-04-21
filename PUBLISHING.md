## Publishing

### Credentials

The library is published to Maven Central by the firebase-sonatype account, Googlers can find the
password for this account in [Valentine](http://valentine/)

### GPG Key

You will need to create a private GPG keyring on your machine, if you don't have one do the
following steps:

  1. Run `gpg --full-generate-key`
  1. Choose `RSA and RSA` for the key type
  1. Use `4096` for the key size
  1. Use `0` for the expiration (never)
  1. Use any name, email address, and password

This creates your key in `~/.gnupg/openpgp-revocs.d/` with `.rev` format. The last 8 characters
before the `.rev` extension are your **Key ID**.

To export the key, run:

```
gpg --export-secret-keys -o $HOME/sonatype.gpg
```

Finally upload your key to the keyserver:

```
gpg --keyserver hkp://keys.openpgp.org --send-keys <YOUR KEY ID>
```

### Local Properties

Open your `$HOME/.gradle/gradle.properties` file at and fill in the values:

```
signing.keyId=<KEY ID>
signing.password=<PASSWORD YOU CHOSE>
signing.secretKeyRingFile=<FULL PATH TO YOUR GPG FILE>
mavenCentralRepositoryUsername=firebase-sonatype
mavenCentralRepositoryUsername=<PASSWORD FROM VALENTINE>
```

### Publish

To publish, run:

```
./gradlew publish
```

### Release

Follow [the instructions here](https://central.sonatype.org/pages/releasing-the-deployment.html):

  1. Navigate to https://s01.oss.sonatype.org/ and **Log In**
  1. On the left side click **Build Promotion** and look for the `com.firebase` repo
  1. Click **Close** ... wait a few minutes (you can check status with **Refresh**)
  1. Click **Release**
