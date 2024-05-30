public class GnomeSort {
    public static void gnomeSort(int[] arr) {
        int n = arr.length;
        int index = 0;

        while (index < n) {
            if (index == 0 || arr[index] >= arr[index - 1]) {
                index++;
            } else {
                int temp = arr[index];
                arr[index] = arr[index - 1];
                arr[index - 1] = temp;
                index--;
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {5, 3, 2, 4, 1};
        gnomeSort(arr);

        // Вывод отсортированного массива
        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}