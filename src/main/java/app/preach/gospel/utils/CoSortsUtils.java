package app.preach.gospel.utils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoSortsUtils {

	private static BufferedImage applyOrientation(final BufferedImage src, final int orientation) {
		// よくあるのは 6(90°) / 3(180°) / 8(270°) / 1(そのまま)
		double theta;
		int dstW = src.getWidth();
		int dstH = src.getHeight();
		final AffineTransform tx = new AffineTransform();
		switch (orientation) {
		case 6: // 90 CW
			theta = Math.toRadians(90);
			dstW = src.getHeight();
			dstH = src.getWidth();
			tx.translate(dstW, 0);
			tx.rotate(theta);
			break;
		case 3: // 180
			theta = Math.toRadians(180);
			tx.translate(dstW, dstH);
			tx.rotate(theta);
			break;
		case 8: // 270 CW (or 90 CCW)
			theta = Math.toRadians(270);
			dstW = src.getHeight();
			dstH = src.getWidth();
			tx.translate(0, dstH);
			tx.rotate(theta);
			break;
		default: // 1 or unknown
			return src;
		}
		final BufferedImage dst = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = dst.createGraphics();
		g2.setTransform(tx);
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		return dst;
	}

	// =========================
	// 2. 稳定归并排序
	// =========================
	// 稳定合并 [lo, mid) 和 [mid, hi)
	private static void mergeAsc(final int[] a, final int lo, final int mid, final int hi, final int[] tmp) {
		// 把要合并的区间复制到 tmp 同位置
		System.arraycopy(a, lo, tmp, lo, hi - lo);
		int i = lo; // 左半部分指针
		int j = mid; // 右半部分指针
		int k = lo; // 原数组写指针
		while (i < mid && j < hi) {
			// 用 <= 保证稳定：左边元素先出
			if (tmp[i] <= tmp[j]) {
				a[k++] = tmp[i++];
			} else {
				a[k++] = tmp[j++];
			}
		}
		while (i < mid) {
			a[k++] = tmp[i++];
		}
		while (j < hi) {
			a[k++] = tmp[j++];
		}
	}

	// 稳定合并 [lo, mid) 和 [mid, hi) => 降序
	private static void mergeDesc(final int[] a, final int lo, final int mid, final int hi, final int[] tmp) {
		System.arraycopy(a, lo, tmp, lo, hi - lo);
		int i = lo, j = mid, k = lo;
		while (i < mid && j < hi) {
			if (tmp[i] >= tmp[j]) { // 改这里！
				a[k++] = tmp[i++];
			} else {
				a[k++] = tmp[j++];
			}
		}
		while (i < mid) {
			a[k++] = tmp[i++];
		}
		while (j < hi) {
			a[k++] = tmp[j++];
		}
	}

	/**
	 * マージソート-昇順
	 *
	 * @param a アレー
	 */
	public static void mergeSortAsc(final int[] a) {
		if (a.length <= 1) {
			return;
		}
		final int[] tmp = new int[a.length];
		mergeSortRecursive(a, 0, a.length, tmp);
	}

	/**
	 * マージソート-降順
	 *
	 * @param a アレー
	 */
	public static void mergeSortDesc(final int[] a) {
		if (a.length <= 1) {
			return;
		}
		final int[] tmp = new int[a.length];
		mergeSortRecursiveDesc(a, 0, a.length, tmp);
	}

	// [lo, hi) 区间归并排序
	private static void mergeSortRecursive(final int[] a, final int lo, final int hi, final int[] tmp) {
		final int size = hi - lo;
		if (size <= 1) {
			return;
		}
		final int mid = (lo + hi) >>> 1;
		mergeSortRecursive(a, lo, mid, tmp);
		mergeSortRecursive(a, mid, hi, tmp);
		// 如果已经有序，则直接跳过 merge
		if (a[mid - 1] <= a[mid]) {
			return;
		}
		mergeAsc(a, lo, mid, hi, tmp);
	}

	// [lo, hi) 区间归并排序 => 降序
	private static void mergeSortRecursiveDesc(final int[] a, final int lo, final int hi, final int[] tmp) {
		final int size = hi - lo;
		if (size <= 1) {
			return;
		}
		final int mid = (lo + hi) >>> 1;
		mergeSortRecursiveDesc(a, lo, mid, tmp);
		mergeSortRecursiveDesc(a, mid, hi, tmp);
		// 如果已经有序，则直接跳过 merge
		if (a[mid - 1] <= a[mid]) {
			return;
		}
		mergeDesc(a, lo, mid, hi, tmp);
	}

	public static BufferedImage readAndNormalizeOrientation(final byte[] jpgBytes) throws Exception {
		// ① 画像読み込み（ImageIO）
		BufferedImage img;
		try (InputStream imgIn = new ByteArrayInputStream(jpgBytes)) {
			img = ImageIO.read(imgIn);
		}
		if (img == null) {
			return null;
		}
		// ② EXIF Orientation 取得
		int orientation = 1;
		try (InputStream metaIn = new ByteArrayInputStream(jpgBytes)) {
			final Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
			final ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (dir != null && dir.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
				orientation = dir.getInt(ExifDirectoryBase.TAG_ORIENTATION);
			}
		} catch (final Exception ignore) {
			// EXIF が無い / 壊れている場合は orientation=1 のまま
		}
		// ③ 回転・反転補正
		return applyOrientation(img, orientation);
	}

	public static BufferedImage readAndNormalizeOrientation(final File jpgFile) throws Exception {
		final BufferedImage img = ImageIO.read(jpgFile);
		if (img == null) {
			return null;
		}
		int orientation = 1;
		try {
			final Metadata metadata = ImageMetadataReader.readMetadata(jpgFile);
			final ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (dir != null && dir.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
				orientation = dir.getInt(ExifDirectoryBase.TAG_ORIENTATION);
			}
		} catch (final Exception ignore) {
			// EXIF 取れない画像もあるので握りつぶしてOK
		}
		return applyOrientation(img, orientation);
	}

	/**
	 * スライスに対して順序を逆転する
	 *
	 * @param arr スライス
	 */
	public static void reverse(final int[] arr) {
		for (int i = 0, j = arr.length - 1; i < j; i++, j--) {
			final int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
	}

	/**
	 * トーナメントソート-昇順
	 *
	 * @param a アレー
	 */
	public static void tournamentSortASc(final int[] arr) {
		if (arr.length <= 1) {
			return;
		}
		int size = 1;
		while (size < arr.length) {
			size <<= 1;
		}
		final int[] tree = new int[2 * size];
		Arrays.fill(tree, Integer.MAX_VALUE);
		for (int i = 0; i < arr.length; i++) {
			tree[size + i] = arr[i];
		}
		for (int i = size - 1; i > 0; i--) {
			tree[i] = Math.min(tree[i << 1], tree[i << 1 | 1]);
		}
		for (int i = 0; i < arr.length; i++) {
			final int minVal = tree[1];
			arr[i] = minVal;
			int idx = 1;
			while (idx < size) {
				if (tree[idx << 1] == minVal) {
					idx <<= 1;
				} else {
					idx = idx << 1 | 1;
				}
			}
			tree[idx] = Integer.MAX_VALUE;
			while (idx > 1) {
				idx >>= 1;
				tree[idx] = Math.min(tree[idx << 1], tree[idx << 1 | 1]);
			}
		}
	}

}
