import cn.breadnicecat.reciperenderer.utils.RRUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PngToTiffConverter {
	public static void main(String[] args) throws IOException {
		// PNG图片路径列表
		File file = new File("C:\\Users\\29012\\IdeaProjects\\RecipeRenderer\\fabric\\run\\rr_export\\20250118_4\\minecraft\\smelting\\bg");
		byte[][] array = Arrays.stream(file.listFiles())
				.map(File::toPath)
				.map(path -> {
					try {
						return Files.readAllBytes(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).toArray(byte[][]::new);
		Files.write(Path.of(".gradle/x.tiff"), RRUtils.tiff(Arrays.asList(array)));
		
	}
}