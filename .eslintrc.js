module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  plugins: ['@typescript-eslint', 'jest', 'import', 'react', 'react-hooks', 'eslint-plugin-prettier'],
  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
    sourceType: 'module',
    tsconfigRootDir: __dirname,
    project: ['./tsconfig.json'],
  },
  env: {
    'jest/globals': true,
    browser: true,
    es6: true,
    node: true,
  },
  extends: [
    'airbnb',
    'airbnb-typescript',
    'plugin:@typescript-eslint/recommended',
    'plugin:@typescript-eslint/recommended-requiring-type-checking',
    'eslint-config-prettier',
    'prettier',
  ],
  rules: {
    // OFF
    'import/prefer-default-export': 0,
    'no-underscore-dangle': 0,
    'spaced-comment': 0,
    'object-shorthand': 0,
    'react/jsx-props-no-spreading': 0,
    'jsx-a11y/click-events-have-key-events': 0,
    'react/jsx-filename-extension': 0,
    'react/jsx-boolean-value': 0,
    'jsx-a11y/no-static-element-interactions': 0,
    'arrow-body-style': 0,
    // Consider
    'no-plusplus': 0,
    'promise/catch-or-return': 0,
    'promise/always-return': 0,
    'react/jsx-one-expression-per-line': 0,
    // Make error later
    'consistent-return': 1,
    'react/prop-types': [
      1,
      {
        ignore: ['children'],
      },
    ],
    'no-shadow': 1,
    'import/no-named-as-default': 1,
    'jsx-a11y/anchor-is-valid': 1,
    'react/no-array-index-key': 1,
    'no-param-reassign': 1,
    'react/forbid-prop-types': 1,
    'import/no-cycle': 1,
    'no-use-before-define': 1,
    //Custom rules
    'prettier/prettier': 2,
    'react-hooks/rules-of-hooks': 2,
    'react-hooks/exhaustive-deps': 2,
    'react/jsx-no-useless-fragment': 1,
    'react/destructuring-assignment': 0,
    'react/react-in-jsx-scope': 0,
    'no-unused-vars': [
      2,
      {
        argsIgnorePattern: '^_',
        ignoreRestSiblings: true,
      },
    ],
    'func-names': 2,
    'react/function-component-definition': [
      2,
      {
        namedComponents: 'arrow-function',
      },
    ],
  },
};
