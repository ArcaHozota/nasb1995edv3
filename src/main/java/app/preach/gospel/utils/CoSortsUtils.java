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

	private static final int INSERTION_SORT_THRESHOLD = 32;

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
	// 1. 非负整数基数排序 (LSD, 基数=256)
	// =========================
	// 对某一字节做稳定的计数排序
	private static void countingPass(final int[] src, final int[] dst, final int[] cnt, final int shift) {
		Arrays.fill(cnt, 0);
		// 统计频次
		for (final int v : src) {
			cnt[(v >>> shift) & 0xFF]++;
		}
		// 前缀和 -> 累计位置
		for (int i = 1; i < 256; i++) {
			cnt[i] += cnt[i - 1];
		}
		// 逆序遍历，保证稳定性
		for (int i = src.length - 1; i >= 0; i--) {
			final int v = src[i];
			dst[--cnt[(v >>> shift) & 0xFF]] = v;
		}
	}

	// 插入排序 [lo, hi)
	private static void insertionSort(final int[] a, final int lo, final int hi) {
		for (int i = lo + 1; i < hi; i++) {
			final int v = a[i];
			int j = i - 1;
			while (j >= lo && a[j] > v) {
				a[j + 1] = a[j];
				j--;
			}
			a[j + 1] = v;
		}
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
	 * 稳定归并排序（top-down），对 int[] 全局排序。 小片段使用插入排序优化。
	 */
	public static void mergeSort(final int[] a) {
		final int n = a.length;
		if (n <= 1) {
			return;
		}
		final int[] tmp = new int[n];
		mergeSortRecursive(a, 0, n, tmp);
	}

	public static void mergeSortDesc(final int[] a) {
		final int[] tmp = new int[a.length];
		mergeSortRecursiveDesc(a, 0, a.length, tmp);
	}

	// [lo, hi) 区间归并排序
	private static void mergeSortRecursive(final int[] a, final int lo, final int hi, final int[] tmp) {
		final int size = hi - lo;
		if (size <= 1) {
			return;
		}
		// 小段用插入排序优化
		if (size <= INSERTION_SORT_THRESHOLD) {
			insertionSort(a, lo, hi);
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
		// 小段用插入排序优化
		if (size <= INSERTION_SORT_THRESHOLD) {
			insertionSort(a, lo, hi);
			return;
		}
		final int mid = (lo + hi) >>> 1;
		mergeSortRecursive(a, lo, mid, tmp);
		mergeSortRecursive(a, mid, hi, tmp);
		// 如果已经有序，则直接跳过 merge
		if (a[mid - 1] <= a[mid]) {
			return;
		}
		mergeDesc(a, lo, mid, hi, tmp);
	}

	/**
	 * 对非负 int 数组进行稳定的基数排序。 使用 8-bit 一轮的 LSD 基数排序 (基数 256)，按字节分三轮或更多轮。 要求：所有元素 >= 0。
	 */
	public static void radixSort(final int[] a) {
		final int n = a.length;
		if (n <= 1) {
			return;
		}
		int max = 0;
		for (final int v : a) {
			if (v < 0) {
				throw new IllegalArgumentException("radixSort only allows non-negative ints");
			}
			if (v > max) {
				max = v;
			}
		}
		if (max == 0) {
			return; // 全是0，已经有序
		}
		final int[] tmp = new int[n];
		final int[] cnt = new int[256];
		int[] src = a;
		int[] dst = tmp;
		int shift = 0;
		// 根据最大值动态决定需要多少轮（每轮 8 bit）
		while ((max >>> shift) != 0) {
			countingPass(src, dst, cnt, shift);
			final int[] t = src;
			src = dst;
			dst = t;
			shift += 8;
		}
		// 如果最后结果在 tmp 中，拷回 a
		if (src != a) {
			System.arraycopy(src, 0, a, 0, n);
		}
	}

	public static void radixSortDesc(final int[] a) {
		radixSort(a);
		reverse(a); // 最快最稳
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

	// =========================
	// 3. smartSort：自动选择算法
	// =========================
	/**
	 * 根据数组长度和元素范围自动选择排序算法： - n <= 32：直接插入排序 - 所有元素 >= 0 且 n 较大：使用
	 * radixSortNonNegative - 否则：使用稳定归并排序
	 *
	 * 这是针对刷题/业务中常见 int 场景的一个简单 heuristics， 重点兼顾「速度 + 稳定性 + 简洁实现」。
	 */
	public static void smartSort(final int[] a) {
		final int n = a.length;
		if (n <= 1) {
			return;
		}
		// 小数组直接插入排序
		if (n <= INSERTION_SORT_THRESHOLD) {
			insertionSort(a, 0, n);
			return;
		}
		boolean hasNegative = a[0] < 0;
		for (int i = 1; i < n; i++) {
			final int v = a[i];
			if (v < 0) {
				hasNegative = true;
				break;
			}
		}
		if (hasNegative) {
			// 含负数：使用稳定归并排序
			mergeSort(a);
		} else {
			// 全是非负数：优先使用基数排序
			// 你实测中基数排序在这种场景非常快
			radixSort(a);
		}
	}

	public static void smartSortDesc(final int[] a) {
		smartSort(a);
		reverse(a);
	}

}
