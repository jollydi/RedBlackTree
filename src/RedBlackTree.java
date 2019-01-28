import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class RedBlackTree<T extends Comparable<? super T>> implements Iterable<RedBlackTree.BinaryNode> {

	public BinaryNode root;
	private int size;
	private int rotationCount;
	private int doubleRotationCount;

	public RedBlackTree() {
		root = null;
		size = 0;
		rotationCount = 0;
		doubleRotationCount = 0;
	}

	public int height() {
		if (isEmpty()) {
			return -1;
		}
		return root.getHeight();
	}

	public int getRotationCount() {
		return rotationCount;
	}

	public boolean isEmpty() {
		return (root == null);
	}

	public int size() {
		return size;
	}

	public Iterator<RedBlackTree.BinaryNode> iterator() {
		return new TreeIterator2(root);
	}

	public boolean insert(T o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
		if (root == null) {
			root = new BinaryNode(o);
			root.color = Color.BLACK;
			size++;
			return true;
		} else {
			MyBoolean bool = new MyBoolean();
			root.insert(o, bool, null, null, null);
			root.color = Color.BLACK;
			return bool.getValue();
		}
	}

	public boolean remove(T o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
		MyBoolean b = new MyBoolean();
		if (root != null) {
			root.removeStep1(o, b, null, null, null);
		} else {
			return false;
		}
		if (root != null) {
			root.color = Color.BLACK;
		}
		return b.getValue();
	}

	public class BinaryNode {

		private T element;
		private Color color;
		private BinaryNode leftChild;
		private BinaryNode rightChild;

		public BinaryNode(T element) {
			this.element = element;
			this.color = Color.RED;
			leftChild = null;
			rightChild = null;
		}

		public int getHeight() {
			int lHeight = -1;
			if (leftChild != null) {
				lHeight = leftChild.getHeight();
			}
			int rHeight = -1;
			if (rightChild != null) {
				rHeight = rightChild.getHeight();
			}
			return (lHeight > rHeight) ? lHeight + 1 : rHeight + 1;
		}

		public void insert(T o, MyBoolean b, BinaryNode P, BinaryNode GP, BinaryNode GGP) {
			if (colorFlip() && successiveReds(P)) {
				String s = handleRotation(P, GP, GGP);
				if (s == "SL" || s == "SR") {
					GP = GGP;
				}
				if (s == "DL" || s == "DR") {
					P = GGP;
					GP = null;
				}
				GGP = null;
			}
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				return;
			}
			if (comparisonResult < 0) {
				if (rightChild == null) {
					rightChild = new BinaryNode(o);
					b.setTrue();
					size++;
					if (rightChild.successiveReds(this)) {
						rightChild.handleRotation(this, P, GP);
					}
					return;
				} else {
					rightChild.insert(o, b, this, P, GP);
				}
			} else {
				if (leftChild == null) {
					leftChild = new BinaryNode(o);
					b.setTrue();
					size++;
					if (leftChild.successiveReds(this)) {
						leftChild.handleRotation(this, P, GP);
					}
					return;
				} else {
					leftChild.insert(o, b, this, P, GP);
				}
			}
			return;
		}

		public String handleRotation(BinaryNode P, BinaryNode GP, BinaryNode GGP) {
			// single right: both reds are left children
			if (P.equals(GP.leftChild) && this.equals(P.leftChild)) {
				if (GGP != null) {
					if (GP.equals(GGP.leftChild)) {
						GGP.leftChild = GP.singleRightRotation();
					} else {
						GGP.rightChild = GP.singleRightRotation();
					}
				} else {
					root = GP.singleRightRotation();
				}
				rotationCount++;
				P.colorChange();
				GP.colorChange();
				return "SR";
			}
			// single left: both reds are right children
			if (P.equals(GP.rightChild) && this.equals(P.rightChild)) {
				if (GGP != null) {
					if (GP.equals(GGP.leftChild)) {
						GGP.leftChild = GP.singleLeftRotation();
					} else {
						GGP.rightChild = GP.singleLeftRotation();
					}
				} else {
					root = GP.singleLeftRotation();
				}
				rotationCount++;
				P.colorChange();
				GP.colorChange();
				return "SL";
			}
			// double right: first red is lc, second red is rc
			if (P.equals(GP.leftChild) && this.equals(P.rightChild)) {
				GP.leftChild = P.singleLeftRotation();
				if (GGP != null) {
					if (GP.equals(GGP.leftChild)) {
						GGP.leftChild = GP.singleRightRotation();
					} else {
						GGP.rightChild = GP.singleRightRotation();
					}
				} else {
					root = GP.singleRightRotation();
				}
				rotationCount += 2;
				this.colorChange();
				GP.colorChange();
				return "DR";
			}
			// double left: first red is rc, second red is lc
			if (P.equals(GP.rightChild) && this.equals(P.leftChild)) {
				GP.rightChild = P.singleRightRotation();
				if (GGP != null) {
					if (GP.equals(GGP.leftChild)) {
						GGP.leftChild = GP.singleLeftRotation();
					} else {
						GGP.rightChild = GP.singleLeftRotation();
					}
				} else {
					root = GP.singleLeftRotation();
				}
				rotationCount += 2;
				this.colorChange();
				GP.colorChange();
				return "DL";
			}
			return null;
		}

		public boolean successiveReds(BinaryNode P) {
			if (P == null) {
				return false;
			}
			return (this.color == Color.RED && P.color == Color.RED);

		}

		public void removeStep1(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			int comparisonResult = compareTo(o);
			if (blackChildren()) {
				color = Color.RED;
				if (comparisonResult == 0) {
					removeStep3(o, b, P, T, GP);
				} else if (comparisonResult < 0) {
					if (rightChild != null) {
						rightChild.removeStep2(o, b, this, leftChild, P);
					}
				} else {
					if (leftChild != null) {
						leftChild.removeStep2(o, b, this, rightChild, P);
					}
				}
			} else {
				removeStep2B(o, b, P, T, GP);
			}
		}

		public void removeStep2(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			if (blackChildren()) {
				removeStep2A(o, b, P, T, GP);
			} else {
				removeStep2B(o, b, P, T, GP);
			}
		}

		public void removeStep2A(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			if (T.blackChildren()) {
				removeStep2A1(o, b, P, T, GP);
			} else if (T.redChildren()) {
				removeStep2A3(o, b, P, T, GP);
			} else if (this.equals(P.leftChild)) {
				if (T.leftChild != null && T.leftChild.color == Color.RED) {
					removeStep2A2(o, b, P, T, GP);
				} else if (T.rightChild != null && T.rightChild.color == Color.RED) {
					removeStep2A3(o, b, P, T, GP);
				}
			} else {
				if (T.redChildren()) {
					removeStep2A3(o, b, P, T, GP);
				} else if (T.rightChild != null && T.rightChild.color == Color.RED) {
					removeStep2A2(o, b, P, T, GP);
				} else if (T.leftChild != null && T.leftChild.color == Color.RED) {
					removeStep2A3(o, b, P, T, GP);
				}
			}

		}

		public void removeStep2A1(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			colorChange();
			P.colorChange();
			T.colorChange();
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				removeStep3(o, b, P, T, GP);
			} else if (comparisonResult < 0) {
				if (rightChild != null) {
					rightChild.removeStep2(o, b, this, leftChild, P);
				}
			} else {
				if (leftChild != null) {
					leftChild.removeStep2(o, b, this, rightChild, P);
				}
			}
		}

		public void removeStep2A2(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			if (GP == null) {
				if (this.equals(P.leftChild)) {
					P.rightChild = T.singleRightRotation();
					root = P.singleLeftRotation();
				} else {
					P.leftChild = T.singleLeftRotation();
					root = P.singleRightRotation();
				}
			} else if (this.equals(P.leftChild)) {
				P.rightChild = T.singleRightRotation();
				if (P.equals(GP.leftChild)) {
					GP.leftChild = P.singleLeftRotation();
				} else {
					GP.rightChild = P.singleLeftRotation();
				}
				rotationCount += 2;
			} else {
				P.leftChild = T.singleLeftRotation();
				if (P.equals(GP.leftChild)) {
					GP.leftChild = P.singleRightRotation();
				} else {
					GP.rightChild = P.singleRightRotation();
				}
				rotationCount += 2;
			}
			colorChange();
			P.colorChange();
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				removeStep3(o, b, P, T, GP);
			} else if (comparisonResult < 0) {
				if (rightChild != null) {
					rightChild.removeStep2(o, b, this, leftChild, P);
				}
			} else {
				if (leftChild != null) {
					leftChild.removeStep2(o, b, this, rightChild, P);
				}
			}
		}

		public void removeStep2A3(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			colorChange();
			P.colorChange();
			T.colorChange();
			if (this.equals(P.leftChild)) {
				T.rightChild.colorChange();
			} else {
				T.leftChild.colorChange();
			}
			if (this.equals(P.leftChild)) {
				if (GP != null) {
					if (P.equals(GP.leftChild)) {
						GP.leftChild = P.singleLeftRotation();
					} else {
						GP.rightChild = P.singleLeftRotation();
					}
				} else {
					root = P.singleLeftRotation();
				}
				rotationCount++;
			} else {
				if (GP != null) {
					if (P.equals(GP.leftChild)) {
						GP.leftChild = P.singleRightRotation();
					} else {
						GP.rightChild = P.singleRightRotation();
					}
				} else {
					root = P.singleRightRotation();
				}
				rotationCount++;
			}
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				removeStep3(o, b, P, T, GP);
			} else if (comparisonResult < 0) {
				if (rightChild != null) {
					rightChild.removeStep2(o, b, this, leftChild, P);
				}
			} else {
				if (leftChild != null) {
					leftChild.removeStep2(o, b, this, rightChild, P);
				}
			}
		}

		public void removeStep2B(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				removeStep3(o, b, P, T, GP);
			} else if (comparisonResult < 0) {
				if (rightChild != null) {
					if (rightChild.color == Color.RED) {
						rightChild.removeStep2B1(o, b, this, leftChild, P);
					} else {
						rightChild.removeStep2B2(o, b, this, leftChild, P);
					}
				}
			} else {
				if (leftChild != null) {
					if (leftChild.color == Color.RED) {
						leftChild.removeStep2B1(o, b, this, rightChild, P);
					} else {
						leftChild.removeStep2B2(o, b, this, rightChild, P);
					}
				}
			}
		}

		public void removeStep2B1(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			int comparisonResult = compareTo(o);
			if (comparisonResult == 0) {
				removeStep3(o, b, P, T, GP);
			} else if (comparisonResult < 0) {
				if (rightChild != null) {
					rightChild.removeStep2(o, b, this, leftChild, P);
				}
			} else {
				if (leftChild != null) {
					leftChild.removeStep2(o, b, this, rightChild, P);
				}
			}
		}

		public void removeStep2B2(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			P.colorChange();
			T.colorChange();
			if (P.equals(root)) {
				if (this.equals(P.leftChild)) {
					root = P.singleLeftRotation();
					GP = T;
					T = T.leftChild;
				} else {
					root = P.singleRightRotation();
					GP = T;
					T = T.rightChild;
				}
				rotationCount++;
			} else if (P.equals(GP.leftChild)) {
				if (this.equals(P.leftChild)) {
					GP.leftChild = P.singleLeftRotation();
					GP = T;
					T = T.leftChild;
				} else {
					GP.leftChild = P.singleRightRotation();
					GP = T;
					T = T.rightChild;
				}
				rotationCount++;
			} else {
				if (this.equals(P.leftChild)) {
					GP.rightChild = P.singleLeftRotation();
					GP = T;
					T = T.leftChild;
				} else {
					GP.rightChild = P.singleRightRotation();
					GP = T;
					T = T.rightChild;
				}
				rotationCount++;
			}

			this.removeStep2(o, b, P, T, GP);
		}

		public void removeStep3(T o, MyBoolean b, BinaryNode P, BinaryNode T, BinaryNode GP) {
			if (leftChild != null && rightChild != null) {
				BinaryNode largest = leftChild.getLargestNode();
				T temp = largest.element;
				if (color == Color.RED) {
					element = temp;
					leftChild.removeStep2(element, b, this, rightChild, P);
				} else {
					removeStep2B(temp, b, P, T, GP);
					this.element = temp;
				}
			} else if (leftChild == null && rightChild == null) {
				if (size == 1) {
					root = null;
				} else if (this.equals(P.leftChild)) {
					P.leftChild = null;
				} else {
					P.rightChild = null;
				}
			} else if (leftChild == null || rightChild == null) {
				if (this.equals(root)) {
					if (leftChild == null) {
						if (rightChild.color == Color.RED) {
							rightChild.color = Color.BLACK;
							root = rightChild;
						}
					} else {
						if (leftChild.color == Color.RED) {
							leftChild.color = Color.BLACK;
							root = leftChild;
						}
					}
				} else if (leftChild == null) {
					if (rightChild.color == Color.RED) {
						rightChild.color = Color.BLACK;
						if (this.equals(P.rightChild)) {
							P.rightChild = rightChild;
						} else {
							P.leftChild = rightChild;
						}
					}
				} else {
					if (leftChild.color == Color.RED) {
						leftChild.color = Color.BLACK;
						if (this.equals(P.rightChild)) {
							P.rightChild = leftChild;
						} else {
							P.leftChild = leftChild;
						}
					}
				}
			}
			b.setTrue();
			size--;
		}

		public BinaryNode getLargestNode() {
			BinaryNode toReturn = this;
			while (toReturn.rightChild != null) {
				toReturn = toReturn.rightChild;
			}
			return toReturn;
		}

		public int compareTo(T o) {
			return element.compareTo(o);
		}

		public BinaryNode singleRightRotation() {
			BinaryNode temp = leftChild.rightChild;
			BinaryNode temp2 = leftChild;
			temp2.rightChild = this;
			if (temp != null) {
				leftChild = temp;
			} else {
				leftChild = null;
			}
			return temp2;
		}

		public BinaryNode singleLeftRotation() {
			BinaryNode temp = rightChild.leftChild;
			BinaryNode temp2 = rightChild;
			temp2.leftChild = this;
			if (temp != null) {
				this.rightChild = temp;
			} else {
				this.rightChild = null;
			}
			return temp2;
		}

		public boolean colorFlip() {
			if (leftChild != null && rightChild != null) {
				if (redChildren() && this.color == Color.BLACK) {
					this.colorChange();
					leftChild.colorChange();
					rightChild.colorChange();
					return true;
				}
			}
			return false;
		}

		public void colorChange() {
			if (color == Color.BLACK) {
				color = Color.RED;
			} else {
				color = Color.BLACK;
			}
		}

		public boolean redChildren() {
			return (leftChild != null && leftChild.color == Color.RED && rightChild != null
					&& rightChild.color == Color.RED);
		}

		public boolean blackChildren() {
			if ((leftChild == null && rightChild == null)
					|| (leftChild != null && rightChild == null && leftChild.color == Color.BLACK)
					|| (rightChild != null && leftChild == null && rightChild.color == Color.BLACK)
					|| (leftChild != null && leftChild.color == Color.BLACK && rightChild != null
							&& rightChild.color == Color.BLACK)) {
				return true;
			}
			return false;
		}

		public T getElement() {
			return element;
		}

		public RedBlackTree<T>.BinaryNode getLeftChild() {
			return leftChild;
		}

		public RedBlackTree<T>.BinaryNode getRightChild() {
			return rightChild;
		}

		public Color getColor() {
			return color;
		}
	}

	private class TreeIterator2 implements Iterator<RedBlackTree.BinaryNode> {

		private Stack<BinaryNode> s;

		public TreeIterator2(BinaryNode b) {
			this.s = new Stack<>();
			if (b != null) {
				s.push(b);
			}
		}

		@Override
		public boolean hasNext() {
			return !s.empty();
		}

		@Override
		public RedBlackTree.BinaryNode next() throws NoSuchElementException {
			if (s.empty()) {
				throw (new NoSuchElementException());
			}
			BinaryNode current = (BinaryNode) s.pop();
			if (current.rightChild != null) {
				s.push(current.rightChild);
			}
			if (current.leftChild != null) {
				s.push(current.leftChild);
			}
			return current;
		}
	}

	private class MyBoolean {

		private boolean value = false;

		public boolean getValue() {
			return value;
		}

		public void setTrue() {
			value = true;
		}
	}

	public enum Color {
		RED, BLACK
	}

	public int getDoubleRotationCount() {
		return doubleRotationCount;
	}
}
