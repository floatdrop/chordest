package chordest.chord;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.labbookpages.WavFile;
import uk.co.labbookpages.WavFileException;
import chordest.beat.BeatRootAdapter;
import chordest.chord.recognition.TemplatesRecognition;
import chordest.properties.Configuration;
import chordest.spectrum.SpectrumData;
import chordest.spectrum.SpectrumFileReader;
import chordest.spectrum.SpectrumFileWriter;
import chordest.transform.CQConstants;
import chordest.transform.PooledTransformer;
import chordest.transform.ScaleInfo;
import chordest.util.DataUtil;
import chordest.util.TuningFrequencyFinder;
import chordest.wave.WaveReader;

/**
 * This class incapsulates all the chord extraction logic. All you need is to
 * pass a name of the wave file as a constructor parameter. When the
 * constructor finishes, all the extracted information is available. It
 * includes the positions of all the beats in the file (extracted with
 * BeatRoot), the positions that were used for recognition (which are
 * essentially the original beat positions supplemented with intermediate
 * points), the array of recognized chords, the recognized mode and the
 * resulting spectrum as a double[][]. 
 * @author Nikolay
 *
 */
public class ChordExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(ChordExtractor.class);
	
	private final double[] originalBeatTimes;
	private final double[] expandedBeatTimes;
	private final Chord[] chords;
	private final double[][] spectrum;
	private final double totalSeconds;
	private final int startNoteOffsetInSemitonesFromF0;
	private final Key key;

	public ChordExtractor(Configuration c, String wavFilePath, String beatFilePath, String spectrumFilePath) {
		BeatRootAdapter beatRoot = new BeatRootAdapter(wavFilePath, beatFilePath);
		originalBeatTimes = beatRoot.getBeatTimes();
		expandedBeatTimes = expand(originalBeatTimes, 3);
		LOG.debug("Transforms: " + expandedBeatTimes.length);
		
		SpectrumData data = readSpectrum(c, wavFilePath, spectrumFilePath, expandedBeatTimes);
		double[][] result = data.spectrum;
		totalSeconds = data.totalSeconds;
		startNoteOffsetInSemitonesFromF0 = data.startNoteOffsetInSemitonesFromF0;
		
		DataUtil.scaleTo01(result);
		
//		String[] labels = NoteLabelProvider.getNoteLabels(startNoteOffsetInSemitonesFromF0, scaleInfo);
//		String[] labels1 = NoteLabelProvider.getNoteLabels(startNoteOffsetInSemitonesFromF0, new ScaleInfo(1, 12));
//		Visualizer.visualizeSpectrum(result, beatTimes, labels, "Spectrum as is");
		
//		double[][] whitened = DataUtil.smoothHorizontally(result, 16);
//		whitened = DataUtil.whitenSpectrum(result, scaleInfo.getNotesInOctaveCount());
//		whitened = DataUtil.removeShortLines(whitened, 8);
//		Visualizer.visualizeSpectrum(whitened, beatTimes, labels, "Whitened spectrum after short lines removal");

		result = DataUtil.smoothHorizontallyMedian(result, c.process.medianFilter1Window); // step 1
		result = DataUtil.filterHorizontal3(result);			// step 2
//		result = DataUtil.removeShortLines(result, 20);			// step 2
		
//		Visualizer.visualizeSpectrum(result, beatTimes, labels, "Prewitt filtered spectrum");

//		double[] e = DataUtil.getSoundEnergyByFrequencyDistribution(result);
//		Visualizer.visualizeXByFrequencyDistribution(e, scaleInfo, startNoteOffsetInSemitonesFromF0);

		result = DataUtil.shrink(result, 8);
//		whitened = DataUtil.shrink(whitened, 8);
		
//		Visualizer.visualizeSpectrum(result, originalBeatTimes, labels, "Prewitt after shrink");
		result = DataUtil.smoothHorizontallyMedian(result, c.process.medianFilter2Window);	// step 3
//		whitened = DataUtil.smoothHorizontally(whitened, 2);
//		Visualizer.visualizeSpectrum(result, originalBeatTimes, labels, "Prewitt after smooth 2");

		double[][] pcp = DataUtil.toPitchClassProfiles(result, c.spectrum.notesPerOctave);
//		double[][] pcp = DataUtil.toPitchClassProfiles(whitened, c.spectrum.notesPerOctave);
		pcp = DataUtil.reduceTo12Notes(pcp);
//		Visualizer.visualizeSpectrum(pcp, originalBeatTimes, labels1, "PCP");
		double[][] rp = DataUtil.getSelfSimilarity(pcp);		// step 6
		rp = DataUtil.getRecurrencePlot(rp, c.process.recurrencePlotTheta, c.process.recurrencePlotMinLength);					// step 6
//		Visualizer.visualizeSelfSimilarity(rp, originalBeatTimes);
		pcp = DataUtil.smoothWithRecurrencePlot(pcp, rp);		// step 6
//		Visualizer.visualizeSpectrum(pcp, originalBeatTimes, labels1, "PCP with RP");

		spectrum = pcp;

		Note startNote = Note.byNumber(startNoteOffsetInSemitonesFromF0);
//		key = Key.recognizeKey(getTonalProfile(pcp, 0, pcp.length), startNote);
		key = null;
		TemplatesRecognition second = new TemplatesRecognition(startNote, key);
//		chords = second.recognize(spectrum, scaleInfo);
		chords = second.recognize(spectrum, new ScaleInfo(1, 12));
	}

	private SpectrumData readSpectrum(Configuration c, String waveFileName,
			String spectrumFileName, double[] expandedBeatTimes) {
		SpectrumData result = new SpectrumData();
		result.beatTimes = expandedBeatTimes;
		result.scaleInfo = new ScaleInfo(c.spectrum.octaves, c.spectrum.notesPerOctave);
		result.startNoteOffsetInSemitonesFromF0 = c.spectrum.offsetFromF0InSemitones;
		result.wavFilePath = waveFileName;
		WavFile wavFile = null;
		try {
			wavFile = WavFile.openWavFile(new File(waveFileName));
			result.samplingRate = (int) wavFile.getSampleRate();
			int frames = (int) wavFile.getNumFrames();
			result.totalSeconds = frames * 1.0 / result.samplingRate;
			
			SpectrumData serialized = SpectrumFileReader.read(spectrumFileName);
			if (result.equalsIgnoreSpectrumAndF0(serialized)) {
				LOG.info("Spectrum was read from " + spectrumFileName);
				return serialized;
			} else {
				result.f0 = TuningFrequencyFinder.getTuningFrequency(waveFileName);
//				result.f0 = CQConstants.F0_DEFAULT;
				CQConstants cqConstants = CQConstants.getInstance(result.samplingRate,
						result.scaleInfo, result.f0, result.startNoteOffsetInSemitonesFromF0);
				int windowSize = cqConstants.getWindowLengthForComponent(0) + 1; // the longest window
				double shift = windowSize / (result.samplingRate * 2.0);
				for (int i = 0; i < result.beatTimes.length; i++) {
					result.beatTimes[i] -= shift;
				}
				WaveReader reader = new WaveReader(wavFile, result.beatTimes, windowSize);
				PooledTransformer transformer = new PooledTransformer(
						reader, result.beatTimes.length, result.scaleInfo, cqConstants);
				result.spectrum = transformer.run();
				if (spectrumFileName != null) {
					SpectrumFileWriter.write(spectrumFileName, result);
				}
				return result;
			}
		} catch (WavFileException e) {
			throw new IllegalArgumentException("Error when reading wave file " + waveFileName, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error when reading wave file " + waveFileName, e);
		} catch (InterruptedException e) {
			throw new IllegalArgumentException("Error when reading wave file " + waveFileName, e);
		} finally {
			if (wavFile != null) { try {
				wavFile.close();
			} catch (IOException ignore) {} }
		}
	}

	public double[] getOriginalBeatTimes() {
		return ArrayUtils.add(originalBeatTimes, totalSeconds);
//		return originalBeatTimes;
	}

	public double[] getBeatTimes() {
		return expandedBeatTimes;
	}

	public Chord[] getChords() {
		return chords;
	}

	public Key getMode() {
		return key;
	}

	public double[][] getSpectrum() {
		return spectrum;
	}

	public double getTotalSeconds() {
		return totalSeconds;
	}

	public int getStartNoteOffsetInSemitonesFromF0() {
		return startNoteOffsetInSemitonesFromF0;
	}

	/**
	 * For each pair of successive elements in an array inserts arithmetic 
	 * mean of them in the middle. This operation is performed for a specified
	 * number of times. The resulting array has length 2^times * (L - 1) + 1
	 * where L is the length of the source array. 
	 * @param array
	 * @param times
	 * @return
	 */
	private double[] expand(double[] array, int times) {
		double[] temp = array;
		for (int i = 0; i < times; i++) {
			temp = DataUtil.expandBeatTimes(temp);
		}
		return temp;
	}

}
