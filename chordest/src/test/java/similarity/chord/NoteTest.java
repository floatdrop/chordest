package similarity.chord;

import junit.framework.Assert;

import org.junit.Test;

public class NoteTest {

	@Test
	public void test_33() {
		Assert.assertEquals(Note.C, Note.byNumber(-33));
	}

	@Test
	public void testOffset() {
		Assert.assertEquals(-1, Note.C.offsetFrom(Note.CD));
		Assert.assertEquals(1, Note.C.offsetFrom(Note.B));
		Assert.assertEquals(2, Note.D.offsetFrom(Note.C));
	}

	@Test
	public void testWithOffset() {
		Assert.assertEquals(Note.C, Note.CD.withOffset(-1));
		Assert.assertEquals(Note.C, Note.CD.withOffset(-121));
		Assert.assertEquals(Note.C, Note.CD.withOffset(11));
		Assert.assertEquals(Note.C, Note.B.withOffset(1));
		Assert.assertEquals(Note.D, Note.C.withOffset(2));
		Assert.assertEquals(Note.D, Note.C.withOffset(122));
	}

}
