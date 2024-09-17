//package cn.breadnicecat.reciperenderer.utils;
//
//import cn.breadnicecat.reciperenderer.includes.org.jetbrains.java.decompiler.main.Fernflower;
//import cn.breadnicecat.reciperenderer.includes.org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
//import cn.breadnicecat.reciperenderer.includes.org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
//import cn.breadnicecat.reciperenderer.includes.org.jetbrains.java.decompiler.main.extern.IResultSaver;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.jar.Manifest;
//
///**
// * Created in 2024/7/25 下午11:57
// * Project: reciperenderer
// *
// * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
// * <p>
// *
// * <p>
// **/
//public class FernFlowerUtils {
//	public static byte[] getClassBytes(Class<?> clazz) throws IOException {
//		String className = clazz.getName().replace('.', '/') + ".class";
//		InputStream is = Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(className));
//		byte[] data = is.readAllBytes();
//		is.close();
//		return data;
//	}
//
//	public static String decompile(Class<?> clazz) throws IOException {
//		return decompile(getClassBytes(clazz), clazz.getName());
//	}
//
//	public static String decompile(byte[] classBytes, String className) {
//		Map<String, Object> options = new HashMap<>();
//		options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
//		options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
//		options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
//
//		final String[] decompiled = new String[1];
//
//		var fernflower = new Fernflower((i, e) -> classBytes, new IResultSaver() {
//			@Override
//			public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
//				decompiled[0] = content;
//			}
//
//			@Override
//			public void saveFolder(String path) {
//			}
//
//			@Override
//			public void copyFile(String source, String path, String entryName) {
//			}
//
//			@Override
//			public void createArchive(String path, String archiveName, Manifest manifest) {
//			}
//
//			@Override
//			public void saveDirEntry(String path, String archiveName, String entryName) {
//			}
//
//			@Override
//			public void copyEntry(String source, String path, String archiveName, String entry) {
//
//			}
//
//			@Override
//			public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
//
//			}
//
//			@Override
//			public void closeArchive(String path, String archiveName) {
//
//			}
//		}, options, new IFernflowerLogger() {
//			@Override
//			public void writeMessage(String message, Severity severity) {
//			}
//
//			@Override
//			public void writeMessage(String message, Severity severity, Throwable t) {
//			}
//		});
//
//		fernflower.addSource(new File(className + ".class"));
//		fernflower.decompileContext();
//
//		return decompiled[0];
//	}
//
//	@Deprecated(forRemoval = true)
//	public static void main(String[] args) throws IOException {
//		System.out.println(decompile(FernFlowerUtils.class));
//	}
//}
