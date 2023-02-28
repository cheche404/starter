package com.cheche.lru;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author cheche
 * @date 2023/2/8
 */
public class LRUService {

  private LRUCache lruCache;

  LRUService(LRUProperties properties) {
    lruCache = new LRUCache(properties.getCapacity());
  }

  public void put(Integer key, Integer value) {
    lruCache.put(key, value);
  }

  public Integer get(Integer key) {
    return lruCache.get(key);
  }

  public String print() {
    return lruCache.print();
  }

  static class LRUCache {
    //维护位置的LinkedList,set()的时间复杂度O(n),但如果只操作头尾元素，则时间复杂度为O(1)
    private LinkedList<Integer> list;
    //维护键值的HashMap,get()的时间复杂度O(1)
    private HashMap<Integer, Integer> map;
    //缓存的容量
    private int capacity;


    LRUCache(int capacity) {
      this.list = new LinkedList<>();
      this.map = new HashMap<>();
      this.capacity = capacity;
    }

    private Integer get(Integer key) {
      if (map.get(key) == null) {
        //说明缓存中没有该key
        return null;
      }
      //缓存中有该key,则先将该key在链表中删除，再移动到链表的尾部，从而保证头部是最近最久未使用的元素
      list.remove(key);
      list.offer(key);
      return map.get(key);
    }

    private void put(Integer key, Integer value) {
      if (map.get(key) != null) {
        //说明缓存中有该key,先在链表中删除，再移动到尾部
        list.remove(key);
        list.offer(key);
      } else {
        //说明缓存中没有该key,需要往缓存中插入
        if (list.size() == capacity) {
          //说明缓存已经满了
          //删除链表头部元素
          Integer head = list.poll();
          //删除键值対
          map.remove(head);
        }
        //此时缓存没满，或刚删除了头部元素
        list.offer(key);
      }
      //插入map或刷新vaule
      map.put(key, value);
    }

    //输出缓存内元素
    private String print() {
      StringBuilder sb = new StringBuilder();
      for (int i = list.size() - 1; i >= 0; i--) {
        Integer key = list.get(i);
        Integer value = map.get(key);
        sb.append("(").append(key).append(",").append(value).append(")").append("\n");
      }
      return sb.toString();
    }

  }

}
