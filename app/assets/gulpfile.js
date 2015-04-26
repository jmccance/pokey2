var babelify = require('babelify');
var browserify = require('browserify');
var del = require('del');
var eslint = require('gulp-eslint');
var gulp = require('gulp');
var gutil = require('gutil');
var path = require('path');
var rename = require('gulp-regex-rename');
var source = require('vinyl-source-stream');
var watchify = require('watchify');

var src = {
  scripts: 'js/**/*.js',
  libs: [
    'bootstrap',
    'director',
    'flux',
    'jquery',
    'react',
    'react-bootstrap'
  ]
};

// Write output to the Play public directory.
var target = {
  root: '../../public/dist',
  scripts: 'js',
  lib: 'lib'
};

var bundler = watchify(browserify('./js/app.js', { debug: true }));
bundler.on('update', bundle);
bundler.on('log', gutil.log);

function bundle() {
  gutil.log('Running bundler...');
  return bundler
    .transform(babelify)
    .bundle()
    .on('error', gutil.log.bind(gutil, 'Browserify Error'))
    .pipe(source('app.js'))
    .pipe(gulp.dest(target.scripts, { cwd: target.root }));
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

gulp.task('compile', bundle);

gulp.task('lib', function () {
  return gulp
    .src(src.libs, { cwd: 'node_modules' })
    .pipe(gulp.dest(target.lib), { cwd: target.root });
});

gulp.task('default', ['clean', 'lint', 'compile', 'lib']);