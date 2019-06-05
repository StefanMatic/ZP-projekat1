package implementation;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Hex;

import code.GuiException;
import gui.Constants;
import x509.v3.CodeV3;
import x509.v3.GuiV3;

public class MyCode extends CodeV3 {
	private MyKeyStorage myKeyStore;
	private int skipCerts = Integer.MAX_VALUE;
	private String selectedCertificate;

	// *******CONSTANTS********
	// Version
	public static final int V3 = 2;
	// Public key algorithms
	public static final int NUM_OF_ALGORITMS = 4;
	public static final int EC = 3;
	// Type
	public static final String TYPE = "pkcs12";

	public MyCode(boolean[] algorithm_conf, boolean[] extensions_conf, boolean extensions_rules) throws GuiException {
		super(algorithm_conf, extensions_conf, extensions_rules);

		if (myKeyStore == null)
			myKeyStore = new MyKeyStorage();

		selectedCertificate = null;
	}

	@Override
	public boolean canSign(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportCSR(String arg0, String arg1, String arg2) {
		// argo0 - file; arg1 - keypair_name; arg2 - algorithm
		try {
			X509Certificate cert = (X509Certificate) myKeyStore.getCertificate(arg1);

			// Pravljenje CSR (Certificate Signing Request)
			JcaX509CertificateHolder holder;
			holder = new JcaX509CertificateHolder(cert);
			JcaPKCS10CertificationRequestBuilder certificationRequestBuilder = new JcaPKCS10CertificationRequestBuilder(
					holder.getSubject(), cert.getPublicKey());

			PrivateKey privateKey = (PrivateKey) myKeyStore.getKey(arg1, myKeyStore.getPassword().toCharArray());
			JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(arg2);
			ContentSigner contentSigner = contentSignerBuilder.build(privateKey);

			org.bouncycastle.pkcs.PKCS10CertificationRequest pkcs10CertificationRequest = certificationRequestBuilder
					.build(contentSigner);

			Writer writer = new FileWriter(arg0);
			JcaPEMWriter pemWriter = new JcaPEMWriter(writer);

			pemWriter.writeObject(pkcs10CertificationRequest);
			pemWriter.flush();
			pemWriter.close();
			return true;
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean exportCertificate(String arg0, String arg1, int arg2, int arg3) {
		// arg0 - file; arg1 - keypairName; arg2 - encoding; arg3 - format
		try {
			X509Certificate cert = (X509Certificate) myKeyStore.getCertificate(arg1);
			X509Certificate[] certChain = (X509Certificate[]) myKeyStore.getCertificateChain(arg0);
			Writer writer = new FileWriter(arg0);

			if (arg2 == Constants.PEM) {
				JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
				if (arg3 == 0) {
					pemWriter.writeObject(cert);
				} else {
					for (X509Certificate c : certChain) {
						pemWriter.writeObject(c);
					}
				}
				pemWriter.flush();
				pemWriter.close();
			} else if (arg2 == Constants.DER) {
				FileOutputStream fos = new FileOutputStream(arg0);
				if (arg3 == 0) {
					fos.write(cert.getEncoded());
				} else {
					for (X509Certificate c : certChain) {
						fos.write(c.getEncoded());
					}
				}
				fos.close();
			}
			writer.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean exportKeypair(String arg0, String arg1, String arg2) {
		// arg0 - keypairName; arg1 - file; arg2 - password

		if (!arg1.endsWith(".p12")) {
			GuiV3.reportError("Pogresan format. Unesite .p12 na kraju imena fajla.");
			return false;
		}

		return myKeyStore.exportKeypair(arg0, arg1, arg2);
	}

	@Override
	public String getCertPublicKeyAlgorithm(String arg0) {
		try {
            return myKeyStore.getCertificate(arg0).getPublicKey().getAlgorithm();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public String getCertPublicKeyParameter(String arg0) {
		try {
            return myKeyStore.getCertificate(arg0).getSigAlgName();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
		// arg0 - keypairName; arg1 - file; arg2 - password
		return myKeyStore.importKeypair(arg0, arg1, arg2);
	}

	@Override
	public int loadKeypair(String arg0) {
		selectedCertificate = arg0;
		X509Certificate myCertificate = myKeyStore.getCertificate(selectedCertificate);

		try {
			JcaX509CertificateHolder holder = new JcaX509CertificateHolder(myCertificate);
			X500Name subject = holder.getSubject();
			X500Name issuer = holder.getIssuer();

			if (issuer != null) {
				super.access.setIssuer(issuer.toString());

				System.out.println(myCertificate.getSigAlgName());
				super.access.setIssuerSignatureAlgorithm(myCertificate.getSigAlgName());
			}

			super.access.setVersion(V3);
			super.access.setPublicKeyAlgorithm("EC");
			super.access.setNotAfter(myCertificate.getNotAfter());
			super.access.setNotBefore(myCertificate.getNotBefore());
			super.access.setSerialNumber(myCertificate.getSerialNumber().toString());

			String selectedSerialNumber = myCertificate.getSerialNumber().toString();
			for (RDN rdn : subject.getRDNs()) {
				AttributeTypeAndValue first = rdn.getFirst();

				if (first.getType().equals(BCStyle.CN)) {
					super.access.setSubjectCommonName(first.getValue().toString());
				} else if (first.getType().equals(BCStyle.C)) {
					super.access.setSubjectCountry(first.getValue().toString());
				} else if (first.getType().equals(BCStyle.ST)) {
					super.access.setSubjectState(first.getValue().toString());
				} else if (first.getType().equals(BCStyle.O)) {
					super.access.setSubjectOrganization(first.getValue().toString());
				} else if (first.getType().equals(BCStyle.L)) {
					super.access.setSubjectLocality(first.getValue().toString());
				} else if (first.getType().equals(BCStyle.OU)) {
					super.access.setSubjectOrganizationUnit(first.getValue().toString());
				} else if (first.getValue().equals(BCStyle.SERIALNUMBER)) {
					selectedSerialNumber = first.getValue().toString();
				}
			}

			// **********************************************************

			// *************Subject key Identifier**************
			if (myCertificate != null) {
				byte[] extension1Value = myCertificate.getExtensionValue(Extension.subjectKeyIdentifier.getId());

				if (extension1Value != null) {
					ASN1OctetString skiOc = ASN1OctetString.getInstance(extension1Value);
					SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(skiOc.getOctets());
					ByteBuffer wrapper = ByteBuffer.wrap(ski.getKeyIdentifier());
					long sCerts = wrapper.getLong();
					super.access.setSubjectKeyID(String.valueOf(sCerts));
					super.access.setEnabledSubjectKeyID(true);
					super.access.setCritical(Constants.SKID,
							holder.getExtension(Extension.subjectKeyIdentifier).isCritical());
				}

			}
			// *************************************************

			// **************Issuer Alternative Name************
			if (myCertificate != null) {
				byte[] extension2Value = myCertificate.getExtensionValue(Extension.issuerAlternativeName.getId());
				if (extension2Value != null) {
					super.access.setCritical(Constants.IAN,
							holder.getExtension(Extension.issuerAlternativeName).isCritical());

					LinkedList alternativeNames = new LinkedList();
					Collection myAlternativeNames = myCertificate.getIssuerAlternativeNames();

					if (myAlternativeNames != null) {
						Iterator iter = myAlternativeNames.iterator();
						List list;

						while (iter.hasNext()) {
							list = (List) iter.next();
							String name = (String) list.get(1);

							alternativeNames.add(name);
						}

						StringBuilder sb = new StringBuilder();
						Iterator ii = alternativeNames.listIterator();
						while (ii.hasNext()) {
							sb.append(ii.next());
							sb.append(" ");
						}

						super.access.setAlternativeName(Constants.IAN, sb.toString());
					}
				}
			}
			// *************************************************

			// ***********Inhibit anyPolicy**********************
			if (myCertificate != null) {
				byte[] extension3Value = myCertificate.getExtensionValue(Extension.inhibitAnyPolicy.getId());
				if (extension3Value != null) {
					super.access.setInhibitAnyPolicy(true);
					super.access.setCritical(Constants.IAP,
							holder.getExtension(Extension.inhibitAnyPolicy).isCritical());

					ASN1Primitive anyPolicy = JcaX509ExtensionUtils.parseExtensionValue(extension3Value);

					System.out.println("*********");
					System.out.println(anyPolicy.toString());

					super.access.setSkipCerts(anyPolicy.toString());
				}
			}

			// *************************************************

			System.out.println("*********8");
			System.out.println(myCertificate.getPublicKey().getAlgorithm());
			
			if (myKeyStore.isCertificateEntry(selectedCertificate)) {
				return 2;
			} else if (myCertificate.getIssuerX500Principal().getName()
					.equals(myCertificate.getSubjectX500Principal().getName())) {
				return 0;
			} else {
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	@Override
	public Enumeration<String> loadLocalKeystore() {
		if (myKeyStore == null)
			myKeyStore = new MyKeyStorage();

		return myKeyStore.getAliases();
	}

	@Override
	public boolean removeKeypair(String arg0) {
		if (myKeyStore != null) {
			myKeyStore.removeKeyPair(arg0);
			return true;
		}
		return false;
	}

	@Override
	public void resetLocalKeystore() {
		if (myKeyStore != null) {
			myKeyStore.resetKeyStore();
		}

	}

	@Override
	public boolean saveKeypair(String arg0) {

		// Provera verzije i obavestenje o gresci
		if (access.getVersion() != Constants.V3) {
			GuiV3.reportError("Wrong version");
			return false;
		}

		try {
			ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(super.access.getPublicKeyECCurve());
			// .getInstance(algorithm, provider)
			KeyPairGenerator myGenerator = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
			myGenerator.initialize(ecGenSpec);
			KeyPair pair = myGenerator.generateKeyPair();
			PublicKey myPublicKey = pair.getPublic();
			PrivateKey myPrivateKey = pair.getPrivate();

			// Dohvatanje svih podataka iz GUI-a
			String country = super.access.getSubjectCountry();
			String state = super.access.getSubjectState();
			String locality = super.access.getSubjectLocality();
			String organization = super.access.getSubjectOrganization();
			String organizationUnit = super.access.getSubjectOrganizationUnit();
			String commonName = super.access.getSubjectCommonName();

			Date notBefore = super.access.getNotBefore();
			Date notAfter = super.access.getNotAfter();

			// Pravljenje sertifikata
			X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
			if (!country.equals(""))
				nameBuilder.addRDN(BCStyle.C, country);
			if (!state.equals(""))
				nameBuilder.addRDN(BCStyle.ST, state);
			if (!locality.equals(""))
				nameBuilder.addRDN(BCStyle.L, locality);
			if (!organization.equals(""))
				nameBuilder.addRDN(BCStyle.O, organization);
			if (!organizationUnit.equals(""))
				nameBuilder.addRDN(BCStyle.OU, organizationUnit);
			if (!commonName.equals(""))
				nameBuilder.addRDN(BCStyle.CN, commonName);

			X500Name holder = nameBuilder.build();
			BigInteger serialNumber = new BigInteger(super.access.getSerialNumber());

			X509v3CertificateBuilder generator = new JcaX509v3CertificateBuilder(holder, serialNumber, notBefore,
					notAfter, holder, myPublicKey);

			// ******Subject key identifier*******
			boolean extension1Critical = super.access.isCritical(Constants.SKID);
			boolean extension1Enabled = super.access.getEnabledSubjectKeyID();

			if (extension1Enabled) {
				SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(myPublicKey.getEncoded());
				SubjectKeyIdentifier subjectKeyId = new SubjectKeyIdentifier(keyInfo.getEncoded());

				generator.addExtension(Extension.subjectKeyIdentifier, extension1Critical, subjectKeyId);
			}
			// ***********************************

			// *******Issuer alternative name*****
			boolean extendion2Critical = super.access.isCritical(Constants.IAN);
			String[] extension2Arg = super.access.getAlternativeName(Constants.IAN);

			if (extension2Arg.length != 0) {
				GeneralName[] generalName = new GeneralName[extension2Arg.length];
				System.out.println(extension2Arg.length);

				for (int i = 0; i < extension2Arg.length; i++) {
					String name = extension2Arg[i];
					System.out.println(name);

					// Provaliti zasto mi ne da da stavim GeneralName.otherName
					// Nabaviti dobre regex-e
					int gName = GeneralName.dNSName;

					System.out.println(gName);
					// DNS name
					if (name.matches("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$")) {
						System.out.println("DNS");
						gName = GeneralName.dNSName;
					}
					// RFC822 name
					if (name.matches(
							"(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?"
									+ "[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)"
									+ "(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]"
									+ "|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:"
									+ "(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?"
									+ "[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\"
									+ "\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+"
									+ "(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?"
									+ "[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\""
									+ "(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-"
									+ "\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\"
									+ "000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\."
									+ "(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\]"
									+ "(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:"
									+ "[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:"
									+ "\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\"
									+ "000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)"
									+ "?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\"
									+ "000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\"
									+ "000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?"
									+ "[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?="
									+ "[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\"
									+ "[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)")) {
						System.out.println("RFC822");
						gName = GeneralName.rfc822Name;
					}
					// X400 name
					if (name.matches("x400:([a-z]*=.*?\\\\;)*(;|$)")) {
						System.out.println("x400");
						gName = GeneralName.x400Address;
					}
					// Directory name
					if (name.matches("^(\\w+\\.?)*\\w+$ ")) {
						System.out.println("direcotry name");
						gName = GeneralName.directoryName;
					}
					// URI - Uniform Resource Identifier
					if (name.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
						System.out.println("URI");
						gName = GeneralName.uniformResourceIdentifier;
					}

					// dodati jos dodataka
					generalName[i] = new GeneralName(gName, name);
				}
				generator.addExtension(Extension.issuerAlternativeName, extendion2Critical,
						new GeneralNames(generalName));

			}

			// ***********************************

			// *******Inhibit anyPolicy***********
			boolean extension3Critical = super.access.isCritical(Constants.IAP);
			boolean extension3enable = super.access.getInhibitAnyPolicy();
			if (extension3enable) {
				if (skipCerts == -1)
					this.skipCerts = Integer.MAX_VALUE;

				String extension3SkipCerts = super.access.getSkipCerts();
				BigInteger skipCertsBigInt = new java.math.BigInteger(extension3SkipCerts);
				ASN1Integer asnInt = new ASN1Integer(skipCertsBigInt);

				generator.addExtension(Extension.inhibitAnyPolicy, extension3Critical, asnInt);
			}
			// ***********************************

			ContentSigner signer = new JcaContentSignerBuilder(access.getPublicKeyDigestAlgorithm())
					.build(myPrivateKey);

			X509CertificateHolder holderr = generator.build(signer);
			X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holderr);
			// certificate.verify(myPublicKey);
			X509Certificate[] chain = new X509Certificate[1];
			chain[0] = certificate;

			PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(myPrivateKey, chain);
			myKeyStore.setEntry(arg0, privateKeyEntry, myKeyStore.getPasswordProtection());
			return true;

		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertIOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
