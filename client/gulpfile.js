var babelify = require('babelify');
var browserify = require('browserify');
var del = require('del');
var eslint = require('gulp-eslint');
var gulp = require('gulp');
var gutil = require('gutil');
var _ = require('lodash');
var path = require('path');
var rename = require('gulp-regex-rename');
var source = require('vinyl-source-stream');
var watchify = require('watchify');

var src = {
  scripts: 'js/**/*.js',
  libs: [
    'node_modules/bootstrap/**/*',
    'node_modules/director/**/*',
    'node_modules/flux/**/*',
    'node_modules/jquery/**/*',
    'node_modules/react/**/*',
    'node_modules/react-bootstrap/**/*'
  ]
};

// Write output to the Play public directory.
var target = {
  root: '../public/dist',
  scripts: 'js',
  lib: 'lib'
};

var bundlerOpts = _.assign({}, watchify.args, { debug: true });

var bundler = browserify('./js/app.js', bundlerOpts).transform(babelify);
bundler.on('log', gutil.log);

function bundleWith(bundler) {
  return function () {
    gutil.log('Running bundler...');
    return bundler
      .bundle()
      .on('error', gutil.log.bind(gutil, 'Browserify Error'))
      .pipe(source('app.js'))
      .pipe(gulp.dest(target.scripts, { cwd: target.root }));
  }
}

// Tasks ////////////////////////////////////////////////////////////

gulp.task('clean', function(cb) {
  del([
   target + '/*'
  ],
  { force: true },
  cb);
});

gulp.task('lint', function () {
  return gulp.src(src.scripts)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

gulp.task('compile', bundleWith(bundler));

gulp.task('watch-compile', function () {
  var watchingBundler = watchify(bundler);
  var bundleF = bundleWith(watchingBundler);
  watchingBundler.on('update', bundleF);
  return bundleF();
});

gulp.task('lib', function () {
  return gulp
    .src(src.libs, { base: 'node_modules' })
    .pipe(gulp.dest(target.lib, { cwd: target.root }));
});

gulp.task('default', ['clean', 'lint', 'compile', 'lib']);

gulp.task('watch', ['clean', 'lint', 'watch-compile', 'lib']);
