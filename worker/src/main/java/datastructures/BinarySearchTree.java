package datastructures;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/**
 * Implementation of a generic Binary Search Tree in which whenever are
 * duplicate keys, values are stored in a list.
 * 
 * @author Christian Gallo Pelaez
 * @author Sebastian Garcia Acosta
 * @param <K,V>, any class that implements the Comparable interface
 */
public class BinarySearchTree<K extends Comparable<K>, V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3509254229421105483L;

	/**
	 * This class represents the node of the BST.
	 * 
	 * The wild-card Node<K,V extends Comparable<? super K,V>> allows K,V to be a
	 * type that is a sub-type of some type that implements Comparable
	 * 
	 * @param <K,V>, any class that implements the Comparable interface
	 */
	@SuppressWarnings("hiding")
	protected class Node<K extends Comparable<? super K>, V> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3073345467094967068L;

		/** Parent node */
		protected Node<K, V> parent;

		/** Left child */
		protected Node<K, V> left;

		/** Right child */
		protected Node<K, V> right;

		/** V, the data that the Node encapsulates */
		protected BigInteger values;

		/** K, the key */
		protected K key;

		protected int height;

		/**
		 * The data that Node encapsulates
		 * 
		 * @param data, an object of a class K,V that implements Comparable interface
		 */
		public Node(K key, V data) {
			this.values = BigInteger.ZERO;
			this.values = this.values.add((BigInteger) data);
			this.left = null;
			this.right = null;
			this.parent = null;
			this.key = key;
			this.height = 0;
		}
	}

	/** Root node of the tree */
	protected Node<K, V> root;

	protected int size;

	/**
	 * Constructor
	 */
	public BinarySearchTree() {
		this.root = null;
		this.size = 0;
	}

	/**
	 * @param root
	 * @param space
	 */
	public void print2DUtil(Node<K, V> root, int space) {
		int count = 5;
		// Base case
		if (root == null)
			return;

		// Increase distance between levels
		space += count;

		// Process right child first
		print2DUtil(root.right, space);

		// Print current node after space
		// count
		for (int i = count; i < space; i++)
			System.out.print(" ");
		System.out.print(root.key.toString() + " " + root.values + "\n");

		// Process left child
		print2DUtil(root.left, space);
	}

	// Wrapper over print2DUtil()
	public void print2D() {
		// Pass initial space count as 0
		print2DUtil(root, 0);
	}

	public String string2DUtil(Node<K, V> root, int space, String s) {
		int count = 5;
		// Base case
		if (root == null)
			return "";

		// Increase distance between levels
		space += count;

		// Process right child first
		print2DUtil(root.right, space);

		// Print current node after space
		// count
		for (int i = count; i < space; i++)
			s += " ";
		s += root.values + "\n";

		// Process left child
		return string2DUtil(root.left, space, s);
	}

	// Wrapper over print2DUtil()
	public String string2D() {
		// Pass initial space count as 0
		return string2DUtil(root, 0, "");
	}

	/**
	 * Searches an element in the tree
	 * 
	 * @param data, K,V
	 * @return Node<K,V> {@link Node}, the node that contains the data searched. If
	 *         not found, returns null
	 */
	protected Node<K, V> searchNode(K key) {
		Node<K, V> nodeFound = root;
		boolean found = false;
		if (root != null) {
			while (!found && nodeFound != null) {
				if (key.compareTo(nodeFound.key) < 0)
					nodeFound = nodeFound.left;
				else if (key.compareTo(nodeFound.key) > 0)
					nodeFound = nodeFound.right;
				else
					found = true;
			}
		}
		if (!found)
			nodeFound = null;
		return nodeFound;
	}

	/**
	 * Determines whether an element is in the tree
	 * 
	 * @param key
	 * @return boolean, true if the element is within the tree; otherwise, false.
	 */
	public boolean contains(K key) {
		Node<K, V> found = searchNode(key);
		return (found != null);
	}

	public BigInteger search(K key) {
		Node<K, V> found = searchNode(key);
		return (found == null) ? null : found.values;
	}

	/**
	 * 
	 * @param value
	 */
	public void add(K key, V value) {
		if (key == null)
			throw new IllegalArgumentException("Key cannot be null");
		else {
			root = add(root, key, value);
			size++;
		}
	}

	protected Node<K, V> add(Node<K, V> currentNode, K key, V value) {
		if (currentNode == null)
			return new Node<>(key, value);
		int cmp = key.compareTo(currentNode.key);
		if (cmp < 0) {
			currentNode.left = add(currentNode.left, key, value);
		} else if (cmp > 0) {
			currentNode.right = add(currentNode.right, key, value);
		} else {
			currentNode.values = currentNode.values.add(BigInteger.ONE); // Add duplicate element at the beginning in order to avoid extra
												// iterations
		}
		return currentNode;
	}

	public void delete(K key) {
		if (key == null)
			throw new IllegalArgumentException("Null key to delete");
		root = delete(root, key);
	}

	protected Node<K, V> delete(Node<K, V> node, K key) {
		int cmp = key.compareTo(node.key);
		if (cmp < 0) {
			node.left = delete(node.left, key);
		} else if (cmp > 0) {
			node.right = delete(node.right, key);
		} else {
			if (node.left == null) {
				return node.right;
			} else if (node.right == null) {
				return node.left;
			} else {
				Node<K, V> y = node;
				node = getMinimum(y.right);
				node.right = deleteMin(y.right);
				node.left = y.left;
			}
			size--;
		}
		return node;
	}

	public int size() {
		return size;
	}

	public void deleteMin() {
		if (isEmpty())
			throw new NoSuchElementException("called deleteMin() with empty symbol table");
		root = deleteMin(root);
	}

	protected Node<K, V> deleteMin(Node<K, V> x) {
		if (x.left == null)
			return x.right;
		x.left = deleteMin(x.left);
		x.height = 1 + Math.max(height(x.left), height(x.right));
		return x;
	}

	public Node<K, V> getMaximum(Node<K, V> localRoot) {
		Node<K, V> currentNode = localRoot;
		while (currentNode.right != null)
			currentNode = currentNode.right;
		return currentNode;
	}

	public Node<K, V> getMinimum(Node<K, V> localRoot) {
		Node<K, V> currentNode = localRoot;
		while (currentNode.left != null)
			currentNode = currentNode.left;
		return currentNode;
	}

	/**
	 * Returns the height of the internal AVL tree. It is assumed that the height of
	 * an empty tree is -1 and the height of a tree with just one node is 0.
	 * 
	 * @return the height of the internal AVL tree
	 */
	public int height() {
		return height(root);
	}

	protected int height(Node<K, V> x) {
		if (x == null)
			return -1;
		return x.height;
	}

	public boolean isEmpty() {
		return root == null;
	}

	public BigInteger getRootData() {
		return this.root.values;
	}

	public void reset() {
		this.root = null;
	}

	public List<String> inorder() {
		List<String> list = new ArrayList<>();
		inorder(root, list);
		return list;
	}

	private void inorder(Node<K, V> root, List<String> list) {
		if (root != null) {
			inorder(root.left, list);
			list.add(root.key.toString() + " " + root.values.toString());
			inorder(root.right, list);
		}
	}

	// public List<V> preorder() {
	// 	List<V> list = new ArrayList<>();
	// 	preorder(root, list);
	// 	return list;
	// }

	// private void preorder(Node<K, V> root, List<V> list) {
	// 	if (root != null) {
	// 		for (V value : root.values)
	// 			list.add(value);
	// 		preorder(root.left, list);
	// 		preorder(root.right, list);
	// 	}
	// }

	// public List<V> preorderLookUp(String key, int maxSize) {
	// 	List<V> list = new LinkedList<>();
	// 	preorderLookUp(root, list, key.toString(), maxSize);
	// 	return list;
	// }

	// private void preorderLookUp(Node<K, V> root, List<V> list, String key, int maxSize) {

	// 	if (root != null) {

	// 		key = key.toUpperCase();

	// 		int keySize = key.length();
	// 		String currKey = root.key.toString().toUpperCase();
			
	// 		String subCurrKey = currKey;
	// 		if(keySize <= currKey.length()) {
	// 			subCurrKey = currKey.substring(0, keySize).toUpperCase();
	// 		}
			

	// 		if (list.size() >= maxSize) {
	// 			list = list.subList(0, maxSize);
	// 			return;
	// 		}

	// 		if (subCurrKey.equalsIgnoreCase(key)) {
	// 			list.addAll(root.values);
	// 		}

	// 		if ((int) key.compareTo(subCurrKey) <= 0) {
	// 			preorderLookUp(root.left, list, key, maxSize);
	// 		}

	// 		if ((int) key.compareTo(subCurrKey) >= 0) {
	// 			preorderLookUp(root.right, list, key, maxSize);
	// 		}

	// 	}
	// }

	// public List<V> postorder() {
	// 	List<V> list = new ArrayList<>();
	// 	postorder(root, list);
	// 	return list;
	// }

	// private void postorder(Node<K, V> root, List<V> list) {
	// 	if (root != null) {
	// 		postorder(root.left, list);
	// 		postorder(root.right, list);
	// 		for (V value : root.values)
	// 			list.add(value);
	// 	}
	// }

	/**
	 * Rotates the given subtree to the right.
	 * 
	 * @param x the subtree
	 * @return the right rotated subtree
	 */
	protected Node<K, V> rotateRight(Node<K, V> x) {
		Node<K, V> y = x.left;
		x.left = y.right;
		y.right = x;
		x.height = 1 + Math.max(height(x.left), height(x.right));
		y.height = 1 + Math.max(height(y.left), height(y.right));
		return y;
	}

	/**
	 * Rotates the given subtree to the left.
	 * 
	 * @param x the subtree
	 * @return the left rotated subtree
	 */
	protected Node<K, V> rotateLeft(Node<K, V> x) {
		Node<K, V> y = x.right;
		x.right = y.left;
		y.left = x;
		x.height = 1 + Math.max(height(x.left), height(x.right));
		y.height = 1 + Math.max(height(y.left), height(y.right));
		return y;
	}

	// @Override
	// public Iterator<List<V>> iterator() {
	// 	return new InorderIterator();
	// }

	/* Iterator */
	// private class InorderIterator implements Iterator<List<V>> {
	// 	/** The nodes that are still to be visited. */
	// 	private Stack<Node<K, V>> stack;

	// 	/** Construct. */
	// 	private InorderIterator() {
	// 		stack = new Stack<Node<K, V>>();
	// 		pushPathToMin(root);
	// 	}

	// 	/**
	// 	 * Push all the nodes in the path from a given node to the leftmost node in the
	// 	 * subtree.
	// 	 */
	// 	private void pushPathToMin(Node<K, V> localRoot) {
	// 		Node<K, V> current = localRoot;
	// 		while (current != null) {
	// 			stack.push(current);
	// 			current = current.left;
	// 		}
	// 	}

	// 	/** Is there another element in this iterator? */
	// 	public boolean hasNext() {
	// 		return !stack.isEmpty();
	// 	}

	// 	/**
	// 	 * The next element in this iterator; Advance the iterator.
	// 	 */
	// 	public List<V> next() {
	// 		Node<K, V> node = stack.peek();
	// 		stack.pop();
	// 		pushPathToMin(node.right);
	// 		return node.values;
	// 	}

	// 	/** (Don't) remove an element from this iterator. */
	// 	public void remove() {
	// 		throw new UnsupportedOperationException();
	// 	}
	// }

	@Override
	public String toString() {
		return string2D();
	}
}
