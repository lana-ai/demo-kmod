/*
 * Copyright 2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.lana.aws;

import ws.salient.chat.Aliases;
import ws.salient.chat.Entity;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Service implements Entity, Serializable {

    private static final long serialVersionUID = 1L;
    
    private Aliases aliases = new Aliases();
    private String name;

    public Service() {
    }

    public Service(String name) {
        this.name = name;
        aliases.withAlias(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Service withAlias(String alias) {
        aliases.withAlias(alias);
        return this;
    }
    
    @Override
    public Aliases getAliases() {
        return aliases;
    }

    @Override
    public List<String> getTypes() {
        return Arrays.asList("Service");
    }
    
}
