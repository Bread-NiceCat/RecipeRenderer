package cn.breadnicecat.reciperenderer.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created in 2025/1/26 19:26
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>, kimi
 * <p>
 *
 * <p>
 **/

public class StageTimer {
	private final Node root = new Node("root", null);
	
	public long getTimeUsed() {
		return root.isEnd() ? root.timeUsed : (getTime() - root.start);
	}
	
	public Node push(String name) {
		return root.push(name);
	}
	
	public void end() {
		root.close();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		root.get(sb, new LinkedList<>(), false);
		return sb.toString();
	}
	
	protected static long getTime() {
		return System.currentTimeMillis();
	}
	
	public boolean isEnd() {
		return root.isEnd();
	}
	
	
	public static class Node implements AutoCloseable {
		private final Node parent;
		private long timeUsed = -1;
		private final List<Node> children = new LinkedList<>();
		
		public final String name;
		public final long start = getTime();
		
		Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
			if (parent != null) {
				if (parent.isEnd()) {
					throw new IllegalStateException("已经终止的Timer无法再推进");
				}
				parent.children.add(this);
			}
		}
		
		
		public Node push(String name) {
			return new Node(name, this);
		}
		
		@Override
		public void close() {
			if (isEnd()) throw new RuntimeException("该节点已终止");
			timeUsed = getTime() - start;
			for (Node child : children) {
				if (!child.isEnd()) {
					child.close();
				}
			}
		}
		
		boolean isTop() {
			return parent == null;
		}
		
		boolean isEnd() {
			return timeUsed != -1;
		}
		
		void get(StringBuilder sb, LinkedList<Character> holders, boolean isLast) {
			if (!isTop()) sb.append("\n");
			
			holders.forEach(sb::append);
			
			if (!isTop()) {
				if (isLast) {
					sb.append("┗");
					holders.addLast(' ');
				} else {
					sb.append("┣");
					holders.addLast('┃');
				}
			}
			
			sb.append(name);
			sb.append(":");
			if (isEnd()) {
				sb.append(timeUsed);
				sb.append("ms");
			} else sb.append("⏱");
			
			ListIterator<Node> iterator = children.listIterator();
			while (iterator.hasNext()) {
				Node next = iterator.next();
				next.get(sb, holders, !iterator.hasNext());
			}
			if (!isTop()) holders.removeLast();
		}
		
		@Override
		public String toString() {
			return isTop() ? name : parent + "." + name;
		}
	}
}