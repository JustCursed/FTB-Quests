package tech.funkyra.ftb;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static com.mongodb.client.model.Filters.eq;

public class DBUtil {
	public static Document toDocument(NBTTagCompound nbt) {
		Document document = new Document();
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(nbt, byteArrayOutputStream);
			String base64 = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
			document.put("nbt", base64);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document;
	}

	public static NBTTagCompound fromDocument(Document document) {
		NBTTagCompound nbt = new NBTTagCompound();
		try {
			byte[] data = Base64.getDecoder().decode(document.getString("nbt"));
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			nbt = CompressedStreamTools.readCompressed(byteArrayInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nbt;
	}
}
