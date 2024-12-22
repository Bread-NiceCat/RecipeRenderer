package cn.breadnicecat.reciperenderer.utils;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created in 2024/12/22 01:30
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 分析类关系,不支持类实现的接口
 * <p>
 **/
public class ClassRelationMap<Root, T> {
	final Node root = new Node(Object.class, null);
	
	public T put(Class<? extends Root> key, T value) {
		Node node = root;
		for (Class<?> c : classes(key)) {
			if (c == Object.class) continue;
			node = node.children.computeIfAbsent(c, Node::new);
		}
		T v0 = node.value;
		node.value = value;
		return v0;
	}
	
	public T setDefault(T value) {
		T v0 = root.value;
		root.value = value;
		return v0;
	}
	
	public T get(Class<? extends Root> key) {
		List<Class<?>> list = classes(key);
		Node neatest = root;
		for (Class<?> c : list) {
			Node node = neatest.children.get(c);
			if (node == null) {
				break;
			}
			neatest = node;
		}
		return neatest.value;
	}
	
	/**
	 * @return 从从Object类(不包含)到参数c类(包含)的有序类集
	 */
	private static List<Class<?>> classes(Class<?> c) {
		ArrayList<Class<?>> list = new ArrayList<>();
		list.add(c);
		Class<?> sup;
		while ((sup = c.getSuperclass()) != null) {
			if (sup != Object.class) list.add(sup);
		}
		return Lists.reverse(list);
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
	
	@Override
	public int hashCode() {
		return root.hashCode();
	}
	
	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof ClassRelationMap<?, ?> that)) return false;
		return root.equals(that.root);
	}
	
	private class Node {
		final Class<?> clazz;
		final Map<Class<?>, Node> children = new HashMap<>();
		@Nullable T value;
		
		Node(Class<?> clazz, @Nullable T value) {
			this.clazz = clazz;
			this.value = value;
		}
		
		Node(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ClassRelationMap<?, ?>.Node node)) return false;
			return Objects.equals(clazz, node.clazz)
					&& Objects.equals(value, node.value)
					&& children.equals(node.children);
		}
		
		@Override
		public int hashCode() {
			int result = Objects.hashCode(clazz);
			result = 31 * result + children.hashCode();
			result = 31 * result + Objects.hashCode(value);
			return result;
		}
		
		@Override
		public String toString() {
			return "Node{%s->%s, children=%s}".formatted(clazz, value, children);
		}
	}
}
