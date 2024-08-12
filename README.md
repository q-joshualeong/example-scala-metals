### Install latest neovim
```
brew install neovim
```
### Install Lazy.nvim

**Lazy.nvim** is a modern plugin manager for Neovim.

#### Lazy.nvim Prerequisites

1. Install Luarocks:
``` bash
wget https://luarocks.org/releases/luarocks-3.11.1.tar.gz
tar zxpf luarocks-3.11.1.tar.gz
cd luarocks-3.11.1
./configure && make && sudo make install
```

#### Set Up Lazy.nvim

This setup follows the structured config setup, which is the recommended way to configure Lazy.nvim.

Your nvim folder structure should look like this:
```bash
nvim
├── init.lua
└── lua
    ├── config
    │   └── lazy.lua
    ├── plugins
    └── settings.lua
```

Create the following configuration files:
 
```lua
-- ~/.config/nvim/init.lua
-- Lazy.nvim plugin manager
require("config.lazy")
```

```lua
-- ~/.config/nvim/lua/config/lazy.lua"
-- Bootstrap lazy.nvim
local lazypath = vim.fn.stdpath("data") .. "/lazy/lazy.nvim"
if not (vim.uv or vim.loop).fs_stat(lazypath) then
  local lazyrepo = "https://github.com/folke/lazy.nvim.git"
  local out = vim.fn.system({ "git", "clone", "--filter=blob:none", "--branch=stable", lazyrepo, lazypath })
  if vim.v.shell_error ~= 0 then
    vim.api.nvim_echo({
      { "Failed to clone lazy.nvim:\n", "ErrorMsg" },
      { out, "WarningMsg" },
      { "\nPress any key to exit..." },
    }, true, {})
    vim.fn.getchar()
    os.exit(1)
  end
end
vim.opt.rtp:prepend(lazypath)

-- Make sure to setup `mapleader` and `maplocalleader` before
-- loading lazy.nvim so that mappings are correct.
-- This is also a good place to setup other settings (vim.opt)
vim.g.mapleader = " "
vim.g.maplocalleader = "\\"

-- Setup lazy.nvim
require("lazy").setup({
  spec = {
    -- import your plugins
    { import = "plugins" },
  },
  -- Configure any other settings here. See the documentation for more details.
  -- colorscheme that will be used when installing plugins.
  install = { colorscheme = { "habamax" } },
  -- automatically check for plugin updates
  checker = { enabled = true },
})
```

### Install nvim-metals 

**Requirements**: Java 17

1. Add the nvim-metals plugin with the [minimal starter config](https://github.com/scalameta/nvim-metals/discussions/39).
    - **IMPORTANT**: Take note of line 76. For nested subprojects, you need to set `project_nesting` to 2 or more depending on how nested your project is.
    - Mappings for LSP can be found from lines 102-117. You can add your own or tweak the existing ones.
    - This config includes some dependencies. The only compulsory one is `nvim-lua/plenary.nvim`. If you don't want the others or already have those plugins configured separately, feel free to remove them.

	```lua
	-- ~/.config/nvim/lua/plugins/nvim-metals.lua
	local map = vim.keymap.set
	local fn = vim.fn
	
	return {
	{
	  "hrsh7th/nvim-cmp",
	  event = "InsertEnter",
	  dependencies = {
	    { "hrsh7th/cmp-nvim-lsp" },
	    { "hrsh7th/cmp-vsnip" },
	    { "hrsh7th/vim-vsnip" }
	  },
	  opts = function()
	    local cmp = require("cmp")
	    local conf = {
	      sources = {
	        { name = "nvim_lsp" },
	        { name = "vsnip" },
	      },
	      snippet = {
	        expand = function(args)
	          -- Comes from vsnip
	          fn["vsnip#anonymous"](args.body)
	        end,
	      },
	      mapping = cmp.mapping.preset.insert({
	        -- None of this made sense to me when first looking into this since there
	        -- is no vim docs, but you can't have select = true here _unless_ you are
	        -- also using the snippet stuff. So keep in mind that if you remove
	        -- snippets you need to remove this select
	        ["<CR>"] = cmp.mapping.confirm({ select = true })
	      })
	    }
	    return conf
	  end
	},
	{
	  "scalameta/nvim-metals",
	  dependencies = {
	    "nvim-lua/plenary.nvim",
	    {
	      "j-hui/fidget.nvim",
	      opts = {},
	    },
	    {
	      "mfussenegger/nvim-dap",
	      config = function(self, opts)
	        -- Debug settings if you're using nvim-dap
	        local dap = require("dap")
	
	        dap.configurations.scala = {
	          {
	            type = "scala",
	            request = "launch",
	            name = "RunOrTest",
	            metals = {
	              runType = "runOrTestFile",
	              --args = { "firstArg", "secondArg", "thirdArg" }, -- here just as an example
	            },
	          },
	          {
	            type = "scala",
	            request = "launch",
	            name = "Test Target",
	            metals = {
	              runType = "testTarget",
	            },
	          },
	        }
	      end
	    },
	  },
	  ft = { "scala", "sbt", "java" },
	  opts = function()
	    local metals_config = require("metals").bare_config()
	    metals_config.find_root_dir_max_project_nesting = 2
	
	    -- Example of settings
	    metals_config.settings = {
	      showImplicitArguments = true,
	      excludedPackages = { "akka.actor.typed.javadsl", "com.github.swagger.akka.javadsl" },
	    }
	
	    -- *READ THIS*
	    -- I *highly* recommend setting statusBarProvider to either "off" or "on"
	    --
	    -- "off" will enable LSP progress notifications by Metals and you'll need
	    -- to ensure you have a plugin like fidget.nvim installed to handle them.
	    --
	    -- "on" will enable the custom Metals status extension and you *have* to have
	    -- a have settings to capture this in your statusline or else you'll not see
	    -- any messages from metals. There is more info in the help docs about this
	    metals_config.init_options.statusBarProvider = "off"
	
	    -- Example if you are using cmp how to make sure the correct capabilities for snippets are set
	    metals_config.capabilities = require("cmp_nvim_lsp").default_capabilities()
	
	    metals_config.on_attach = function(client, bufnr)
	      require("metals").setup_dap()
	
	      -- LSP mappings
	      map("n", "gD", vim.lsp.buf.definition)
	      map("n", "K", vim.lsp.buf.hover)
	      map("n", "gi", vim.lsp.buf.implementation)
	      map("n", "gr", vim.lsp.buf.references)
	      map("n", "gds", vim.lsp.buf.document_symbol)
	      map("n", "gws", vim.lsp.buf.workspace_symbol)
	      map("n", "<leader>cl", vim.lsp.codelens.run)
	      map("n", "<leader>sh", vim.lsp.buf.signature_help)
	      map("n", "<leader>rn", vim.lsp.buf.rename)
	      map("n", "<leader>f", vim.lsp.buf.format)
	      map("n", "<leader>ca", vim.lsp.buf.code_action)
	
	      map("n", "<leader>ws", function()
	        require("metals").hover_worksheet()
	      end)
	
	      -- all workspace diagnostics
	      map("n", "<leader>aa", vim.diagnostic.setqflist)
	
	      -- all workspace errors
	      map("n", "<leader>ae", function()
	        vim.diagnostic.setqflist({ severity = "E" })
	      end)
	
	      -- all workspace warnings
	      map("n", "<leader>aw", function()
	        vim.diagnostic.setqflist({ severity = "W" })
	      end)
	
	      -- buffer diagnostics only
	      map("n", "<leader>d", vim.diagnostic.setloclist)
	
	      map("n", "[c", function()
	        vim.diagnostic.goto_prev({ wrap = false })
	      end)
	
	      map("n", "]c", function()
	        vim.diagnostic.goto_next({ wrap = false })
	      end)
	
	      -- Example mappings for usage with nvim-dap. If you don't use that, you can
	      -- skip these
	      map("n", "<leader>dc", function()
	        require("dap").continue()
	      end)
	
	      map("n", "<leader>dr", function()
	        require("dap").repl.toggle()
	      end)
	
	      map("n", "<leader>dK", function()
	        require("dap.ui.widgets").hover()
	      end)
	
	      map("n", "<leader>dt", function()
	        require("dap").toggle_breakpoint()
	      end)
	
	      map("n", "<leader>dso", function()
	        require("dap").step_over()
	      end)
	
	      map("n", "<leader>dsi", function()
	        require("dap").step_into()
	      end)
	
	      map("n", "<leader>dl", function()
	        require("dap").run_last()
	      end)
	    end
	
	    return metals_config
	  end,
	  config = function(self, metals_config)
	    local nvim_metals_group = vim.api.nvim_create_augroup("nvim-metals", { clear = true })
	    vim.api.nvim_create_autocmd("FileType", {
	      pattern = self.ft,
	      callback = function()
	        require("metals").initialize_or_attach(metals_config)
	      end,
	      group = nvim_metals_group,
	    })
	  end
	}
	}
	
	```


### Importing a Project

1. Navigate to the project directory.
2. Ensure you are using Java 17 (I recommend using [sdkman](https://sdkman.io/) to manage your Java versions).
3. Create a dummy Scala file at the project root:
	```bash
	touch dummy.scala
	```
4. Open the dummy file with Neovim and wait for nvim-metals to prompt for project import. Select option 1.
	```
	New Gradle workspace detected, would you like to import the build?:
	1: Import build
	2: Not now
	3: Don't show again
	Type number and <Enter> or click with the mouse (q or empty cancels):
	```
5. Follow the progress of the project import:
	```bash
	tail -f .metals/metals.log
	```
6. After the import is successful, delete the dummy file.
7. Test the `goto definition (gD)` and `find references (gr)` on some of your files. The first time you open these files, they will need to be indexed and compiled, but this should be fast.
8. Add the following to your `.gitignore`:
```
.bloop/*
.metals/*
```

