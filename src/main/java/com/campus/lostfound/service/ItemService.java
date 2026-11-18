
package com.campus.lostfound.service;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemService {

    private final ConcurrentHashMap<Long, Item> items =
            new ConcurrentHashMap<>();

    private final AtomicLong counter = new AtomicLong();

    public Item addItem(Item item) {
        long id = counter.incrementAndGet();

        item.setId(id);
        item.setStatus(ItemStatus.OPEN);

        items.put(id, item);

        return item;
    }

    public List<Item> getAllItems() {
        List<Item> result = new ArrayList<>(items.values());
        result.sort((a, b) -> a.getId().compareTo(b.getId()));
        return result;
    }

    public Item getItemById(Long id) {
        return items.get(id);
    }
}
