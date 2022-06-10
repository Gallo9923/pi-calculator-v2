package datastructures;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import model.Point;

public class AVLBSTree<K extends Comparable<K>, V> extends BinarySearchTree<K, V> implements BalancedBSTree<K, V>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1005016605184816284L;

	public AVLBSTree() {
		super();
	}
	
	public static void main(String[] args) {

		Point.setEpsilonPower(5);
		
		AVLBSTree<Point, BigInteger> avl = new AVLBSTree<>();

		avl.add(new Point(0.5, 0.5), BigInteger.ONE);
		avl.add(new Point(0.5, 0.5), BigInteger.ONE);
		avl.add(new Point(0.75, 0.75), BigInteger.ONE);
		avl.add(new Point(0.25, 0.25), BigInteger.ONE);
		avl.add(new Point(0.00, 0.00), BigInteger.ONE);
		avl.add(new Point(0.15, 0.15), BigInteger.ONE);
		avl.add(new Point(0.00, 0.00), BigInteger.ONE);
		// avl.add(new Point(0.10, 0.10), BigInteger.ONE);
		// avl.add(new Point(0.05, 0.05), BigInteger.ONE);

		// avl.add("Chris", "Chris");
		// avl.add("Maria", "Maria");
		// avl.add("Juan", "Juan");
		// avl.add("Clau", "Clau");
		// avl.add("Claudio", "Claudio");
		// avl.add("Clausilio", "Clausilio");
		// avl.add("Julia", "Julia");
		// avl.add("CZ", "CZ");
		// avl.add("CZZZZZZZZZZZZZ", "CZZZZZZZZZZZZZ");
		avl.print2D();
		
		System.out.println("==============");
		System.out.println(avl.inorder());
		System.out.println("Repeated count = " + avl.getSum());
		// System.out.println(avl.preorderLookUp("JU", 100));
		
		// System.out.println(avl.search("Clau"));
	}

	@Override
	protected Node<K, V> add(Node<K, V> node, K key, V value) {
		Node<K, V> x = super.add(node, key, value);
		x.height = 1 + Math.max(height(x.left), height(x.right));
		return balance(x);
	}

	@Override
	protected Node<K, V> delete(Node<K, V> node, K key) {
		Node<K, V> x = super.delete(node, key);
		if(x != null) { // leaf node case
			x.height = 1 + Math.max(height(x.left), height(x.right));						
			return balance(x);
		} else
			return null;
	}

	/**
	 * Restores the AVL tree property of the subtree.
	 * 
	 * @param x the subtree
	 * @return the subtree with restored AVL property
	 */
	private Node<K, V> balance(Node<K, V> x) {
		if (balanceFactor(x) < -1) {
			if (balanceFactor(x.right) > 0) {
				x.right = rotateRight(x.right);
			}
			x = rotateLeft(x);
		} else if (balanceFactor(x) > 1) {
			if (balanceFactor(x.left) < 0) {
				x.left = rotateLeft(x.left);
			}
			x = rotateRight(x);
		}
		return x;
	}

	/**
	 * Returns the balance factor of the subtree. The balance factor is defined as
	 * the difference in height of the left subtree and right subtree, in this
	 * order. Therefore, a subtree with a balance factor of -1, 0 or 1 has the AVL
	 * property since the heights of the two child subtrees differ by at most one.
	 * 
	 * @param x the subtree
	 * @return the balance factor of the subtree
	 */
	protected int balanceFactor(Node<K, V> x) {
		return height(x.left) - height(x.right);
	}

	public BigInteger getSum() {
		if (root == null) {
			return BigInteger.ZERO;
		} else {
			BigInteger left = getSum(root.left);
			BigInteger right = getSum(root.right);
			BigInteger result = BigInteger.ZERO;
			if (left.compareTo(BigInteger.ONE) > 0) {
				result = result.add(left);
			}
			if (right.compareTo(BigInteger.ONE) > 0) {
				result = result.add(right);
			}
			if (root.values.compareTo(BigInteger.ONE) > 0) {
				result = result.add(root.values);
			}
			return result;
		}
	}

	private BigInteger getSum(Node<K, V> x) {
		if (x == null) {
			return BigInteger.ZERO;
		} else {
			BigInteger left = getSum(x.left);
			BigInteger right = getSum(x.right);
			BigInteger result = BigInteger.ZERO;
			if (left.compareTo(BigInteger.ONE) > 0) {
				result = result.add(left);
			}
			if (right.compareTo(BigInteger.ONE) > 0) {
				result = result.add(right);
			}
			if (x.values.compareTo(BigInteger.ONE) > 0) {
				result = result.add(x.values);
			}
			return result;
		}
	}

	// @Override
	// public List<V> autoComplete(String key, int maxSize) {
	// 	return preorderLookUp(key, maxSize);
	// }
}