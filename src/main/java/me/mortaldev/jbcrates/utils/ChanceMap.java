package me.mortaldev.jbcrates.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import me.mortaldev.jbcrates.Main;

/**
 * v1.0.1
 *
 * <p>A map-like data structure that associates keys with BigDecimal chances (weights). It provides
 * methods for rolling items based on their chances and for balancing the chances so they sum up to
 * 100.00.
 *
 * <p>
 *
 * @param <T> The type of the keys in the map.
 */
public class ChanceMap<T> {
  private LinkedHashMap<T, BigDecimal> table;

  @JsonCreator
  public ChanceMap(LinkedHashMap<T, BigDecimal> initialTable) {
    this.table = new LinkedHashMap<>(initialTable);
  }

  /** Constructs an empty ChanceMap. */
  public ChanceMap() {
    this.table = new LinkedHashMap<>();
  }

  /**
   * Tells Jackson to serialize this ChanceMap object *as if* it were just the internal map.
   *
   * @return The internal map for Jackson serialization.
   */
  @JsonValue
  public LinkedHashMap<T, BigDecimal> getTableForSerialization() {
    return (this.table == null) ? new LinkedHashMap<>() : this.table;
  }

  public boolean isEmpty() {
    return table.isEmpty();
  }

  /**
   * Returns a synchronized copy of the internal table.
   *
   * @return A new LinkedHashMap containing the current entries of the ChanceMap.
   */
  @JsonIgnore
  public synchronized LinkedHashMap<T, BigDecimal> getTable() {
    return (this.table == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(this.table);
  }

  /**
   * Sets the internal table to the provided LinkedHashMap. This operation is synchronized.
   *
   * @param table The new LinkedHashMap to set as the internal table.
   */
  @JsonIgnore
  public synchronized void setTable(LinkedHashMap<T, BigDecimal> table) {
    this.table = table;
  }

  private <K, V> LinkedHashMap<K, V> reverseMap(LinkedHashMap<K, V> original) {
    LinkedHashMap<K, V> reversed = new LinkedHashMap<>();
    ListIterator<Map.Entry<K, V>> iterator =
        new ArrayList<>(original.entrySet()).listIterator(original.size());

    while (iterator.hasPrevious()) {
      Map.Entry<K, V> entry = iterator.previous();
      reversed.put(entry.getKey(), entry.getValue());
    }

    return reversed;
  }

  /**
   * Returns the number of entries in the ChanceMap.
   *
   * @return The size of the internal table.
   */
  public int size() {
    return table.size();
  }

  /**
   * Original roll method. This version uses a random number between 0 and 100, which assumes the
   * table's chances sum to 100 for reliable item picking.
   */
  @Deprecated
  public T roll() {
    if (table.isEmpty()) {
      return null;
    }
    double generatedNumber = ThreadLocalRandom.current().nextDouble(0.00, 100.00);
    sort();
    LinkedHashMap<T, BigDecimal> reversedMap = reverseMap(table);
    BigDecimal total = BigDecimal.ZERO;
    for (Map.Entry<T, BigDecimal> entry : reversedMap.entrySet()) {
      if (entry.getValue() == null) continue;
      total = total.add(entry.getValue());
      if (BigDecimal.valueOf(generatedNumber).compareTo(total) < 0) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Helper method to pick an item from a map sorted by values in ascending order. The randomNumber
   * should be generated in the range [0, sumOfPositiveWeightsInMap).
   */
  private T pickFromSortedAscendingMap(
      LinkedHashMap<T, BigDecimal> sortedAscendingMap, BigDecimal randomNumber) {
    BigDecimal cumulativeWeight = BigDecimal.ZERO;
    for (Map.Entry<T, BigDecimal> entry : sortedAscendingMap.entrySet()) {
      BigDecimal currentWeight = entry.getValue();
      if (currentWeight != null && currentWeight.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal nextCumulativeWeight = cumulativeWeight.add(currentWeight);
        if (randomNumber.compareTo(nextCumulativeWeight) < 0) {
          return entry.getKey();
        }
        cumulativeWeight = nextCumulativeWeight;
      }
    }
    return null;
  }

  /**
   * Rolls for a specified number of rewards from the chance map.
   *
   * @param numberOfRewards The number of rewards to roll for.
   * @param allowDuplicates If true, the same item can be rolled multiple times. If false, each
   *     rolled item is unique for this call.
   * @return A list of rolled items. The list might be smaller than numberOfRewards if not enough
   *     unique items are available (when allowDuplicates is false) or if the table has no positive
   *     chances.
   */
  public List<T> roll(int numberOfRewards, boolean allowDuplicates) {
    if (this.table == null || this.table.isEmpty() || numberOfRewards <= 0) {
      return Collections.emptyList();
    }

    List<T> rewards = new ArrayList<>();

    if (allowDuplicates) {
      // Sort this.table once (descending by value) as per original roll() logic.
      // This has a side effect of sorting the main table.
      this.sort();
      LinkedHashMap<T, BigDecimal> sortedAscendingTable =
          reverseMap(this.table); // Now ascending by value

      BigDecimal sumOfPositiveWeights = BigDecimal.ZERO;
      for (BigDecimal weight : sortedAscendingTable.values()) {
        if (weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
          sumOfPositiveWeights = sumOfPositiveWeights.add(weight);
        }
      }

      if (sumOfPositiveWeights.compareTo(BigDecimal.ZERO) <= 0) {
        return rewards; // No positive weights to roll against
      }

      for (int i = 0; i < numberOfRewards; i++) {
        double randomFactor = ThreadLocalRandom.current().nextDouble(); // [0.0, 1.0)
        BigDecimal randomNumber =
            new BigDecimal(String.valueOf(randomFactor)).multiply(sumOfPositiveWeights);

        T rolledItem = pickFromSortedAscendingMap(sortedAscendingTable, randomNumber);
        if (rolledItem != null) {
          rewards.add(rolledItem);
        } else {
          break;
        }
      }
    } else { // No duplicates
      LinkedHashMap<T, BigDecimal> workingPool = new LinkedHashMap<>(this.table);
      for (int i = 0; i < numberOfRewards; i++) {
        if (workingPool.isEmpty()) {
          break; // No more items to pick from
        }

        // Sort the current workingPool: descending by value, then reverse to get ascending.
        LinkedHashMap<T, BigDecimal> sortedDescPool =
            workingPool.entrySet().stream()
                .sorted(Map.Entry.<T, BigDecimal>comparingByValue().reversed())
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        LinkedHashMap<T, BigDecimal> sortedAscendingPool = reverseMap(sortedDescPool);

        BigDecimal sumOfPositiveWeightsInPool = BigDecimal.ZERO;
        for (BigDecimal weight : sortedAscendingPool.values()) {
          if (weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
            sumOfPositiveWeightsInPool = sumOfPositiveWeightsInPool.add(weight);
          }
        }

        if (sumOfPositiveWeightsInPool.compareTo(BigDecimal.ZERO) <= 0) {
          break; // No more positive weights in the current pool
        }

        double randomFactor = ThreadLocalRandom.current().nextDouble(); // [0.0, 1.0)
        BigDecimal randomNumber =
            new BigDecimal(String.valueOf(randomFactor)).multiply(sumOfPositiveWeightsInPool);

        T rolledItem = pickFromSortedAscendingMap(sortedAscendingPool, randomNumber);

        if (rolledItem != null) {
          rewards.add(rolledItem);
          workingPool.remove(rolledItem); // Remove from pool so it's not picked again
        } else {
          break;
        }
      }
    }
    return rewards;
  }

  /**
   * Updates the chance (value) for a specific key.
   *
   * @param key The key whose value should be updated.
   * @param newValue The new BigDecimal value for the key.
   * @return true if the key was found and updated, false otherwise.
   */
  public boolean updateKey(T key, BigDecimal newValue) {
    if (!table.containsKey(key)) {
      return false;
    }
    table.put(key, newValue);
    return true;
  }

  /**
   * Updates the chance (value) for a specific key using a Number.
   *
   * @param key The key whose value should be updated.
   * @param newValue The new Number value for the key.
   * @return true if the key was found and updated, false otherwise.
   */
  public boolean updateKey(T key, Number newValue) {
    return updateKey(key, new BigDecimal(newValue.toString()));
  }

  public boolean updateKey(T originalKey, T updatedKey) {
    BigDecimal chance = table.remove(originalKey);
    put(updatedKey, chance, false);
    return true;
  }

  /**
   * Updates the chance (value) for a specific key using a String representation of a number.
   *
   * @param key The key whose value should be updated.
   * @param newValue The new String value for the key.
   * @return true if the key was found and updated, false otherwise.
   */
  public boolean updateKey(T key, String newValue) {
    return updateKey(key, new BigDecimal(newValue));
  }

  public boolean balanceTable() {
    if (isBalanced()) {
      return false;
    }

    if (table.isEmpty()) {
      return false;
    }

    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal value : table.values()) {
      if (value != null) {
        sum = sum.add(value);
      }
    }
    if (sum.compareTo(BigDecimal.ZERO) == 0) {
      Main.log("ERROR! '0' found as total sum in ChanceMap during balanceTable");
      return false;
    }

    LinkedHashMap<T, BigDecimal> newTable = new LinkedHashMap<>();
    BigDecimal runningTotalScaled = BigDecimal.ZERO;
    int entryCount = table.size();
    int currentIndex = 0;

    for (Map.Entry<T, BigDecimal> entry : table.entrySet()) {
      BigDecimal originalValue = entry.getValue();
      if (originalValue == null || originalValue.compareTo(BigDecimal.ZERO) < 0) {
        originalValue = BigDecimal.ZERO;
      }

      BigDecimal scaledValue;
      if (currentIndex == entryCount - 1) {
        scaledValue = new BigDecimal("100.00").subtract(runningTotalScaled);
        if (scaledValue.compareTo(BigDecimal.ZERO) < 0) {
          scaledValue = BigDecimal.ZERO;
        }
      } else {
        BigDecimal proportion = originalValue.divide(sum, 10, RoundingMode.HALF_UP);
        scaledValue = proportion.multiply(new BigDecimal("100.00"));
      }
      scaledValue = scaledValue.setScale(2, RoundingMode.HALF_UP);

      newTable.put(entry.getKey(), scaledValue);
      runningTotalScaled = runningTotalScaled.add(scaledValue);
      currentIndex++;
    }

    BigDecimal finalSumCheck = BigDecimal.ZERO;
    for (BigDecimal val : newTable.values()) {
      finalSumCheck = finalSumCheck.add(val);
    }

    if (finalSumCheck.compareTo(new BigDecimal("100.00")) != 0 && !newTable.isEmpty()) {
      BigDecimal difference = new BigDecimal("100.00").subtract(finalSumCheck);
      Map.Entry<T, BigDecimal> firstEntry = newTable.entrySet().iterator().next();
      BigDecimal adjustedValue =
          firstEntry.getValue().add(difference).setScale(2, RoundingMode.HALF_UP);
      if (adjustedValue.compareTo(BigDecimal.ZERO) < 0) adjustedValue = BigDecimal.ZERO;
      newTable.put(firstEntry.getKey(), adjustedValue);
    }

    this.table = newTable;
    return true;
  }

  /**
   * Sorts the internal table in descending order based on the BigDecimal values. This operation is
   * synchronized.
   */
  public synchronized void sort() {
    if (table.isEmpty()) {
      return;
    }

    LinkedHashMap<T, BigDecimal> sortedMap =
        table.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .sorted(Map.Entry.<T, BigDecimal>comparingByValue().reversed())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));
    this.table = new LinkedHashMap<>(sortedMap);
  }

  /**
   * Checks if the sum of all chances in the table is exactly 100.00.
   *
   * @return true if the total sum is 100.00, false otherwise.
   */
  public boolean isBalanced() {
    return getTotal().compareTo(new BigDecimal("100.00")) == 0;
  }

  public synchronized BigDecimal getTotal() {
    if (table.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal value : table.values()) {
      if (value != null) {
        total = total.add(value);
      }
    }
    return total;
  }

  /**
   * Adds a new key to the table with a calculated chance based on the current size. Optionally
   * balances the table after adding. This operation is synchronized.
   *
   * @param key The key to add.
   * @param balanceAfter If true, balance the table after adding the key.
   */
  public synchronized void put(T key, boolean balanceAfter) {
    BigDecimal percent;
    if (table.isEmpty()) {
      percent = new BigDecimal("100.00");
    } else {
      BigDecimal size = new BigDecimal(table.size() + 1);
      BigDecimal hundred = new BigDecimal("100.00");
      percent = hundred.divide(size, 2, RoundingMode.HALF_UP);
    }
    put(key, percent, balanceAfter);
  }

  /**
   * Adds a new key to the table with a specified chance (as a Number). Optionally balances the
   * table after adding. This operation is synchronized.
   *
   * @param key The key to add.
   * @param amount The chance (as a Number) for the key.
   * @param balanceAfter If true, balance the table after adding the key.
   */
  public synchronized void put(T key, Number amount, boolean balanceAfter) {
    BigDecimal decimalAmount = new BigDecimal(amount.toString()).setScale(2, RoundingMode.HALF_UP);
    put(key, decimalAmount, balanceAfter);
  }

  /**
   * Adds a new key to the table with a specified chance (as a BigDecimal). Optionally balances the
   * table after adding. This operation is synchronized.
   *
   * @param key The key to add.
   * @param amount The chance (as a BigDecimal) for the key.
   */
  public synchronized void put(T key, BigDecimal amount, boolean balanceAfter) {
    table.put(key, amount.setScale(2, RoundingMode.HALF_UP));
    if (balanceAfter) {
      balanceTable();
    }
  }

  /**
   * Removes a key from the table. Optionally balances the table after removing. This operation is
   * synchronized.
   *
   * @param key The key to remove.
   * @param balanceAfter If true, balance the table after removing the key.
   */
  public synchronized void remove(T key, boolean balanceAfter) {
    if (table.isEmpty()) {
      return;
    }
    table.remove(key);
    if (balanceAfter && !table.isEmpty()) {
      balanceTable();
    }
  }
}
