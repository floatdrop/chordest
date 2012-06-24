package similarity.chord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Chord {

	public static final String MAJ = "maj";
	public static final String MIN = "min";
	public static final String DIM = "dim";
	public static final String AUG = "aug";
	public static final String MAJ7 = "maj7";
	public static final String MIN7 = "min7";
	public static final String DOM = "7";
	public static final String DIM7 = "dim7";
	public static final String HDIM7 = "hdim7";
	public static final String MINMAJ7 = "minmaj7";
	public static final String MAJ6 = "maj6";
	public static final String MIN6 = "min6";
	public static final String NON =  "9";
	public static final String MAJ9 = "maj9";
	public static final String MIN9 = "min9";
	public static final String SUS2 = "sus2";
	public static final String SUS4 = "sus4";
	public static final String N =  "N";
	public static final String NO_SHORTHAND = "";

	//private Map<Integer, Note> components = new HashMap<Integer, Note>();
	private final Note[] components;
	private final String shorthand;

	public static Chord major(Note tonic) {
		return new Chord(tonic, MAJ);
	}

	public static Chord minor(Note tonic) {
		return new Chord(tonic, MIN);
	}

	public static Chord empty() {
		return new Chord();
	}

	private Chord() {
		this.shorthand = N;
		this.components = new Note[0];
	}

	public Chord(Note... notes) {
		if (notes.length > 0) {
			Set<Note> set = new HashSet<Note>();
			set.addAll(Arrays.asList(notes));
			set.remove(null);
			Note[] array = set.toArray(new Note[set.size()]);
			int i = 0;
			while (i < array.length && array[i] != null) { i++; }
			this.components = Arrays.copyOf(array, i);
		} else {
			this.components = new Note[0];
		}
		this.shorthand = NO_SHORTHAND;
	}

	public Chord(Note root, String shortHand) {
		this.shorthand = String.copyValueOf(shortHand.toCharArray());
		if (MAJ.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH) };
		} else if (MIN.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH) };
		} else if (DIM.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.DIMINISHED_FIFTH) };
		} else if (AUG.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.AUGMENTED_FIFTH) };
		} else if (MAJ7.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MAJOR_SEVENTH) };
		} else if (MIN7.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MINOR_SEVENTH) };
		} else if (DOM.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MINOR_SEVENTH) };
		} else if (DIM7.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.DIMINISHED_FIFTH), root.withOffset(Interval.DIMINISHED_SEVENTH) };
		} else if (HDIM7.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.DIMINISHED_FIFTH), root.withOffset(Interval.MINOR_SEVENTH) };
		} else if (MINMAJ7.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MAJOR_SEVENTH) };
		} else if (MAJ6.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MAJOR_SIXTH) };
		} else if (MIN6.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MAJOR_SIXTH) };
		} else if (NON.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MINOR_SEVENTH), root.withOffset(Interval.NINTH) };
		} else if (MAJ9.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MAJOR_SEVENTH), root.withOffset(Interval.NINTH) };
		} else if (MIN9.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MINOR_THIRD), root.withOffset(Interval.PERFECT_FIFTH), root.withOffset(Interval.MINOR_SEVENTH), root.withOffset(Interval.NINTH) };
		} else if (SUS2.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.MAJOR_SECOND), root.withOffset(Interval.PERFECT_FIFTH) };
		} else if (SUS4.equals(shortHand)) {
			this.components = new Note[] { root, root.withOffset(Interval.PERFECT_FOURTH), root.withOffset(Interval.PERFECT_FIFTH) };
		} else if (N.equals(shortHand)) {
			this.components = new Note[0];
			// do nothing
		} else {
			if (root != null) {
				this.components = new Note[] { root };
			} else {
				this.components = new Note[0];
			}
		}
	}

	/**
	 * @return Modifiable list of notes
	 */
	public List<Note> getNotesAsList() {
		List<Note> result = new ArrayList<Note>();
		for (Note note : this.components) { result.add(note); }
		return result;
	}

	public Note[] getNotes() {
		return this.components;
	}

	private Note getRoot() {
		return getNote(0);
	}

	private Note getNote(int position) {
		if (this.components == null || position < 0 || position >= this.components.length) {
			return null;
		}
		return this.components[position];
	}

	public String getShortHand() {
		return shorthand;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) { return false; }
		if (o == this) { return true; }
		if (!(o instanceof Chord)) { return false; }
		Chord other = (Chord)o;
		if (this.isShortHandDefined() && other.isShortHandDefined()) {
			if (Chord.AUG.equals(this.shorthand) && Chord.AUG.equals(other.shorthand)) {
				return hasSameNotesWith(other);
			} else {
				return (this.getRoot() == other.getRoot()) &&
					(this.shorthand.equals(other.shorthand));
//				|| (MAJ.equals(this.shorthand) && MAJ7.equals(other.shorthand))
//				|| (MAJ7.equals(this.shorthand) && MAJ.equals(other.shorthand))
//				|| (MIN.equals(this.shorthand) && MIN7.equals(other.shorthand))
//				|| (MIN7.equals(this.shorthand) && MIN.equals(other.shorthand)));
			}
		} else {
			return hasSameNotesWith(other);
		}
	}

	private boolean hasSameNotesWith(Chord other) {
		if (this.getNotes().length != other.getNotes().length) { return false; }
		Set<Note> notes = new LinkedHashSet<Note>();
		notes.addAll(this.getNotesAsList());
		if (notes.containsAll(other.getNotesAsList())) {
			return true; // because their sizes are already equal
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (Note note : getNotes()) {
			result += note.hashCode(); 
		}
		return result;
	}

	public boolean hasCommon3Notes(Chord other) {
		List<Note> thisNotes = this.getNotesAsList();
		int common = 0;
		for (Note note : other.getNotes()) {
			if (thisNotes.contains(note)) {
				common++;
			}
		}
		return common >= 3;
	}

	@Override
	public String toString() {
		if (this.isEmpty()) {
			return "N";
		}
		StringBuilder sb = new StringBuilder();
		if (this.shorthand != null && !("".equals(this.shorthand)) && !("N".equals(this.shorthand))) {
			sb.append(this.getRoot().getShortName());
			if (! MAJ.equals(this.shorthand)) {
				sb.append(":");
				sb.append(this.shorthand);
			}
		} else if (this.isMajor()) {
			sb.append(this.getRoot().getShortName());
			sb.append(":maj");
		} else if (this.isMinor()) {
			sb.append(this.getRoot().getShortName());
			sb.append(":min");
		} else {
			for (Note n : this.components) {
				sb.append(n.getShortName());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public boolean hasNote(Note note) {
		for (Note current : this.components) {
			if (current.equals(note)) {
				return true;
			}
		}
		return false;
	}

	public boolean isMajor() {
		if (isShortHandDefined()) {
			return MAJ.equals(shorthand);
		} else {
			return (this.components.length == 3) &&
			((getNote(1).ordinal() - getNote(0).ordinal() + 12) % 12 == Interval.MAJOR_THIRD) &&
			((getNote(2).ordinal() - getNote(0).ordinal() + 12) % 12 == Interval.PERFECT_FIFTH);
		}
	}

	public boolean isMinor() {
		if (isShortHandDefined()) {
			return MIN.equals(shorthand);
		} else {
			return (this.components.length == 3) &&
			((getNote(1).ordinal() - getNote(0).ordinal() + 12) % 12 == Interval.MINOR_THIRD) &&
			((getNote(2).ordinal() - getNote(0).ordinal() + 12) % 12 == Interval.PERFECT_FIFTH);
		}
	}
	
	public boolean isEmpty() {
		return this.components.length == 0 || N.equals(this.shorthand);
	}
	
	private boolean isShortHandDefined() {
		return ! NO_SHORTHAND.equals(shorthand);
	}
}
