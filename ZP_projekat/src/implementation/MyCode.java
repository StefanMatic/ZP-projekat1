package implementation;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.Enumeration;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import code.GuiException;
import x509.v3.CodeV3;

public class MyCode extends CodeV3 {
	private KeyStore keyStore;

	public MyCode(boolean[] algorithm_conf, boolean[] extensions_conf, boolean extensions_rules) throws GuiException {
		super(algorithm_conf, extensions_conf, extensions_rules);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canSign(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportCSR(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportCertificate(String arg0, String arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportKeypair(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCertPublicKeyAlgorithm(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCertPublicKeyParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubjectInfo(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean importCAReply(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String importCSR(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean importCertificate(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean importKeypair(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int loadKeypair(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Enumeration<String> loadLocalKeystore() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeKeypair(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetLocalKeystore() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean saveKeypair(String arg0) {
		try {
			ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(super.access.getPublicKeyECCurve());
			//.getInstance(algorithm, provider)
			KeyPairGenerator myGenerator = KeyPairGenerator.getInstance("ECDSA", new BouncyCastleProvider());
			myGenerator.initialize(ecGenSpec);
			KeyPair pair = myGenerator.generateKeyPair();
			PublicKey myPublicKey = pair.getPublic();
			PrivateKey myPrivateKey = pair.getPrivate();
			
			Date notBefore = super.access.getNotBefore();
			Date notAfter = super.access.getNotAfter();
		
			X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
			nameBuilder.addRDN(BCStyle.C, super.access.getSubjectCountry());
			nameBuilder.addRDN(BCStyle.ST, super.access.getSubjectState());
			nameBuilder.addRDN(BCStyle.L, super.access.getSubjectLocality());
			nameBuilder.addRDN(BCStyle.O, super.access.getSubjectOrganization());
			nameBuilder.addRDN(BCStyle.OU, super.access.getSubjectOrganizationUnit());
			nameBuilder.addRDN(BCStyle.CN, super.access.getSubjectCommonName());
			
			X500Name holder = nameBuilder.build();
			BigInteger serialNumber = new BigInteger(super.access.getSerialNumber());
		
			X509v3CertificateBuilder generator = new JcaX509v3CertificateBuilder(holder, serialNumber, notBefore, notAfter, holder, myPublicKey);
		
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}

	@Override
	public boolean signCSR(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

}
