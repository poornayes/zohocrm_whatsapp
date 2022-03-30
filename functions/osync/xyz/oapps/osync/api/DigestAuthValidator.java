package xyz.oapps.osync.api;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.DigestAuthEntity;

public class DigestAuthValidator {

	public static DigestAuthEntity createNewDigestAuth(String osyncId, String entityType, String entityId,
			Date validUpto, boolean isOtp) throws Exception {
		DigestAuthEntity digest = new DigestAuthEntity();
		digest.setEntityType(entityType);
		digest.setEntityId(entityId);
		digest.setValidUpto(validUpto);
		digest.setOsyncId(osyncId);
		if (isOtp) {
			digest.setDigestValue(randomAlphaNumeric(6));
		} else {
			String digestStr = RequestController.getUUID() + "-" + osyncId + "-" + entityId + validUpto
					+ System.currentTimeMillis();
			String shaDigest = encryptThisString(digestStr);
			digest.setDigestValue(shaDigest);
		}
		return OsyncDB.get(osyncId).insert(digest);
	}

	public static DigestAuthEntity validateDigestAuth(String digest) throws Exception {
		OsyncDB osp = OsyncDB.get();
		SelectQuery<?> query = query().select(field("*")).from("DigestAuth").where(field("digest_value").eq(digest))
				.getQuery();
		DigestAuthEntity digestAuthEntity = osp.findOne(DigestAuthEntity.class, query);
		Date date = new Date();
		if (digestAuthEntity != null) {
			boolean valid = date.before(digestAuthEntity.getValidUpto());
			if (valid) {
				OsyncDB.get(digestAuthEntity.getOsyncId()).deleteByEntity(digestAuthEntity);
				return digestAuthEntity;
			}
		}
		return null;
	}
	
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public static String encryptThisString(String input) {
		try {
			// getInstance() method is called with algorithm SHA-224
			MessageDigest md = MessageDigest.getInstance("SHA-224");

			// digest() method is called
			// to calculate message digest of the input string
			// returned as array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			// Add preceding 0s to make it 32 bit
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}

			// return the HashText
			return hashtext;
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
