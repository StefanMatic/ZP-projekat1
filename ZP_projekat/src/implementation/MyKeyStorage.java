package implementation;

import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.UUID;

public class MyKeyStorage {
	private KeyStore keyStorage;
	private String password;

	public MyKeyStorage() {
		try {
			keyStorage = KeyStore.getInstance("PKCS12");
			keyStorage.load(null, null);
			String uuid = UUID.randomUUID().toString();
			setPassword(uuid);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	X509Certificate getCertificate(String alias) {
		try {
			return (X509Certificate) keyStorage.getCertificate(alias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Certificate[] getCertificateChain(String arg0) {
		try {
			return keyStorage.getCertificateChain(arg0);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean containsAliases(String alias) {
		try {
			return keyStorage.containsAlias(alias);
		}
		catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
			}	
		}
	
	Enumeration<String> getAliases() {
		try {
			return keyStorage.aliases();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void removeKeyPair(String arg0) {
		try {
			keyStorage.deleteEntry(arg0);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void resetKeyStore() {
		try {
			keyStorage.load(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	public boolean isKeyEntry(String string) {
		try {
			return keyStorage.isKeyEntry(string);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Key getKey(String alias, char[] charArray) {
		try {
			return keyStorage.getKey(alias, charArray);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setKeyEntry(String arg0, PrivateKey private1, char[] charArray, Certificate[] chain) {
		try {
			keyStorage.setKeyEntry(arg0, private1, charArray, chain);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public void setSertificate(String alias, X509Certificate certificate) {
		try {
			keyStorage.setCertificateEntry(alias, certificate);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}