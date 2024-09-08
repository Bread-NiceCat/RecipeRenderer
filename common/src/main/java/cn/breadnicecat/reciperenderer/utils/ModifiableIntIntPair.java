package cn.breadnicecat.reciperenderer.utils;

/**
 * Created in 2024/9/8 02:29
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ModifiableIntIntPair {
	private int left, right;
	
	public ModifiableIntIntPair() {
	}
	
	public ModifiableIntIntPair(int left, int right) {
		this.left = left;
		this.right = right;
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getRight() {
		return right;
	}
	
	public void setLeft(int left) {
		this.left = left;
	}
	
	public void setRight(int right) {
		this.right = right;
	}
	
	public void addLeft(int v) {
		this.left += v;
	}
	
	public void addRight(int v) {
		this.right += v;
	}
	
}
