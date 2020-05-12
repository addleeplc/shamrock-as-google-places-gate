/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.as.context.AutocompleteContext;

import java.util.List;

public interface AddressSearchGate extends com.haulmont.shamrock.address.AddressSearchGate {
    List<Address> autocomplete(AutocompleteContext ctx);
}
