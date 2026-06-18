package ca.bkaw.praeter.gui.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Utility for extracting the {@link ResourcePack Resource Pack} containing all
 * vanilla assets.
 */
public final class VanillaAssets {
	private static final String VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
	private static final Logger LOGGER = LoggerFactory.getLogger(VanillaAssets.class);

	/**
	 * Read the vanilla assets, and extract them if they did not exist.
	 *
	 * @param path The path to place the vanilla assets.
	 * @param gameVersion The game version to expect.
	 * @return The resource pack containing the vanilla assets.
	 * @throws IOException If an I/O error occurs.
	 */
	public static ResourcePack readOrExtract(Path path, String gameVersion) throws IOException {
		ResourcePack vanillaAssets = ResourcePack.loadZip(path);
		if (!vanillaAssetsExists(vanillaAssets, gameVersion)) {
			// The vanilla assets didn't exist or were not up to date.
			vanillaAssets.getRoot().getFileSystem().close();
			Files.deleteIfExists(path);

			// Extract
			VanillaAssets.extract(path, gameVersion);
			// Load again
			vanillaAssets = ResourcePack.loadZip(path);
		}
		return vanillaAssets;
	}

	private static boolean vanillaAssetsExists(ResourcePack vanillaAssets, String gameVersion) throws IOException {
		// If the pack does not exist, opening it will create it. But in that case,
		// the version.json file will not exist.
		Path path = vanillaAssets.getPath("version.json");
		if (Files.exists(path)) {
			// The file exists, but let's make sure it's up to date.
			JsonElement json = JsonParser.parseReader(Files.newBufferedReader(path));
			String version = json.getAsJsonObject().get("name").getAsString();
			// If the version matches, the vanilla assets are up to date
			return version.equals(gameVersion);
		}
		return false;
	}

	/**
	 * Extract the vanilla assets.
	 *
	 * @param path The path to extract to.
	 * @param gameVersion The game version to get the assets for.
	 * @throws IOException If an I/O error occurs.
	 */
	public static void extract(Path path, String gameVersion) throws IOException {
		Files.createDirectories(path.getParent());

		LOGGER.info("    Downloading version manifest, version info and client jar");

		// Download the version manifest
		JsonElement versionManifest = JsonParser.parseReader(getVersionManifest());

		JsonArray versions = versionManifest.getAsJsonObject().get("versions").getAsJsonArray();
		String url = null;
		String sha1 = null;
		for (JsonElement versionElement : versions) {
			JsonObject versionJsonObject = versionElement.getAsJsonObject();
			if (gameVersion.equals(versionJsonObject.get("id").getAsString())) {
				url = versionJsonObject.get("url").getAsString();
				sha1 = versionJsonObject.get("sha1").getAsString();
				break;
			}
		}
		if (url == null || sha1 == null) {
			throw new RuntimeException("Unable to find " + gameVersion + " in the version manifest.");
		}

		// Get the version info
		String versionInfoString = get(url, sha1);
		JsonElement versionInfo = JsonParser.parseString(versionInfoString);

		// Get the url to the client jar and download it
		JsonObject clientDownload = versionInfo.getAsJsonObject().get("downloads").getAsJsonObject().get("client").getAsJsonObject();
		Path downloadPath = Files.createTempFile("client", ".jar");
		download(clientDownload.get("url").getAsString(), clientDownload.get("sha1").getAsString(), downloadPath);

		LOGGER.info("    Extracting vanilla assets from client jar");

		FileSystem clientFileSystem = FileSystems.newFileSystem(URI.create("jar:" + downloadPath.toUri()), Map.of());

		ResourcePack resourcePack = ResourcePack.loadZip(path);
		// Copy pack.png
		Files.copy(clientFileSystem.getPath("pack.png"), resourcePack.getPath("pack.png"));
		// Copy version.json to validate the version
		Files.copy(clientFileSystem.getPath("version.json"), resourcePack.getPath("version.json"));

		// Copy assets folder
		Path from = clientFileSystem.getPath("assets");
		Path to = resourcePack.getPath("assets");
		Files.walkFileTree(from, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(to.resolve(from.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, to.resolve(from.relativize(file)));
				return FileVisitResult.CONTINUE;
			}
		});

		LOGGER.info("    Cleaning up");
		clientFileSystem.close();
		Files.delete(downloadPath);

		// Close the resource pack to save the file
		resourcePack.getRoot().getFileSystem().close();

		LOGGER.info("    Done");
	}

	private static InputStreamReader getVersionManifest() throws IOException {
		InputStream in = URI.create(VERSION_MANIFEST_URL).toURL().openStream();
		return new InputStreamReader(in);
	}

	private static String get(String url, String sha1) throws IOException {
		try (InputStream in = URI.create(url).toURL().openStream()) {
			byte[] bytes = in.readAllBytes();
			MessageDigest messageDigest;
			try {
				messageDigest = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				// Should never happen as all Java platforms are required to implement SHA-1
				throw new RuntimeException(e);
			}
			byte[] gotBytes = messageDigest.digest(bytes);
			String gotSha1 = HexFormat.of().formatHex(gotBytes);
			if (!sha1.equalsIgnoreCase(gotSha1)) {
				throw new RuntimeException("Download failed, sha1 hash of downloaded data did not match. Expected: " + sha1 + " Found: " + gotSha1 + " for url " + url);
			}
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}


	private static void download(String url, String sha1, Path path) throws IOException {
		try (InputStream in = URI.create(url).toURL().openStream()) {
			byte[] bytes = in.readAllBytes();
			MessageDigest messageDigest;
			try {
				messageDigest = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				// Should never happen as all Java platforms are required to implement SHA-1
				throw new RuntimeException(e);
			}
			byte[] fileBytes = messageDigest.digest(bytes);
			String fileSha1 = HexFormat.of().formatHex(fileBytes);
			if (!sha1.equalsIgnoreCase(fileSha1)) {
				throw new RuntimeException("Download failed, sha1 hash of downloaded file did not match. Expected: " + sha1 + " Found: " + fileSha1 + " for file " + path);
			}
			Files.write(path, bytes);
		}
	}
}