from cryptography.hazmat.primitives.asymmetric import ed25519
from cryptography.hazmat.primitives import serialization
import getpass
import os

private_key_hex = "1d7f94131a6f1c2a8bdd175dab67e42b22c02817715043d22d338def65e59e73"
passphrase = b"test123"

# Output files will go into current directory
pem_file_path = "account5857246.pem"
pass_file_path = "account5857246.pass"

private_bytes = bytes.fromhex(private_key_hex)
private_key = ed25519.Ed25519PrivateKey.from_private_bytes(private_bytes)

pem = private_key.private_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PrivateFormat.PKCS8,
    encryption_algorithm=serialization.BestAvailableEncryption(passphrase),
)

with open(pem_file_path, "wb") as f:
    f.write(pem)

with open(pass_file_path, "wb") as f:
    f.write(passphrase)

print(f"✅ PEM written to {pem_file_path}")
print(f"🔐 Passphrase saved to {pass_file_path}")
